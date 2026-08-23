# DataNest — Android client

DataNest is a file-sync Android app. Files are picked, shared or captured on the
device, stored locally in Room, and pushed to a Spring backend that keeps
metadata in Postgres and the assets themselves in Cloudinary. The app is
**offline-first**: every action writes to Room immediately and marks the row
pending, and a WorkManager job reconciles with the server afterwards. Nothing in
the UI blocks on the network, and a device that is offline stays fully usable —
its changes queue until a sync succeeds.

The server it talks to is
[pranavgoyal0102/DataNest_Backend](https://github.com/pranavgoyal0102/DataNest_Backend).
That README documents the endpoints, the envelope format and the server side of
the sync protocol; this one covers the client.

## Architecture

Room is the single source of truth. The UI reads it and never reads the network;
`SyncWorker` is the only thing in the app that calls the backend at all — it is
the sole construction site for both `SyncRepo` and `FileRemoteRepository`.

```
  +--------------------------------------------------+
  |  Compose UI            ui/screens, ui/viewmodel   |
  |  observes Flow<List<FileStored>> from Room        |
  +--------------------------------------------------+
       |  writes the row, marks it PENDING_*
       |  and asks for a sync
       v
  +--------------------------------------------------+
  |  Room  "datanest_db"           data/local         |   <-- source of truth
  |  files, folders                                   |
  +--------------------------------------------------+
       |  read by
       v
  +--------------------------------------------------+
  |  SyncRepo / SyncWorker         sync               |   <-- only network caller
  +--------------------------------------------------+
       |
       v
  +--------------------------------------------------+
  |  Retrofit + OkHttp             data/remote        |
  |  AuthInterceptor . TokenAuthenticator             |
  +--------------------------------------------------+
       |
       v
              DataNest_Backend
```

Packages are organised by responsibility rather than by layer name:

| Package | Holds |
| --- | --- |
| `data/local` | Room database, DAOs, entities, converters |
| `data/remote` | Retrofit service, OkHttp auth, upload/download bodies |
| `data/remote/dto` | Wire types, kept separate from the Room entities |
| `sync` | `SyncWorker`, `SyncRepo`, `SyncScheduler`, cursor storage |
| `ui/screens`, `ui/viewmodel`, `ui/icons`, `ui/theme` | Compose |

The Room entity (`FileStored`) and the wire DTO (`FileResponse`) are deliberately
distinct types. The entity carries local-only state — `syncStatus`, the
device-side `uri` — that the server neither sends nor accepts.

## Sync protocol

One sync pass is **push, then pull**, in that order.

### Push

`SyncRepo.syncAllFiles()` runs three phases in sequence, and the order matters:

1. **Uploads** — rows in `LOCAL_ONLY` or `FAILED`. A multipart `POST
   api/files/upload`, streamed straight off the `ContentResolver` so the file
   never fully materialises on the heap. Success stores the server's `id` and
   `version`.
2. **Updates** — rows in `PENDING_UPDATE`. A `PATCH api/files/{id}` carrying
   title, starred, deleted and the last known `version`. Trashing a file is an
   ordinary update with `isDeleted = true`, not a delete.
3. **Purges** — rows in `PENDING_PURGE`, the explicit "delete permanently" from
   the trash screen. A `DELETE api/files/{id}?version=`.

Uploads go first because the phases below them need a `remoteId`; a file created
and renamed before its first sync must exist server-side before the rename can
be pushed.

### Delta pull

`GET api/files/sync?cursor=&limit=200` returns a page of everything changed since
the cursor, and the client keeps requesting while `hasMore` is true, up to 50
pages per run. The cursor is **opaque**: it is whatever the server last handed
back, stored verbatim and never parsed, ordered or reconstructed. The only
operation performed on it is an equality check, to detect a server handing back
the same cursor while still promising more — which would otherwise re-request one
page forever.

The cursor is persisted **only after a page has been applied**, and with
`commit()` rather than `apply()`, because a background sync can be killed the
moment it returns. Any failure — HTTP error, transport fault, or a payload
without the expected shape — returns without advancing it. Advancing past rows
that were never applied would skip them permanently, so a re-fetched page is
always preferred to a skipped one; applying the same page twice lands on the same
state.

### Why the worker is state-driven

`SyncWorker` takes no payload. Its `inputData` carries only a mode flag, and it
syncs **whatever is pending in Room at the moment it runs** — it queries for
`LOCAL_ONLY`, `PENDING_UPDATE` and `PENDING_PURGE` rows itself.

This is what makes a dropped trigger harmless. A trigger is not a message that
can be lost; it is a hint that the database is dirty. If five edits collapse into
one worker run, that run still finds all five rows. If a trigger is dropped
because a run is already queued, the queued run picks the work up. Nothing is
carried in the trigger, so nothing can be lost with it.

Mode is either `incremental` or `full`. `full` means only that the stored cursor
is dropped before pulling, so the next delta starts from scratch; the push phases
are identical either way. The first sync after install is `full`.

## Sync triggers

Three, all funnelling into the same unique work:

| Trigger | Where | Delay |
| --- | --- | --- |
| Any local change — create, rename, star, trash, purge | `RoomViewModel` | 5s |
| App returning to the foreground | `DataNestApp`, via `ProcessLifecycleOwner` | none |
| Manual sync button | `MainNavigation` | none |

Signing in also schedules one.

All three use `enqueueUniqueWork("file_sync", KEEP)` with a `CONNECTED`
constraint. **Coalescing comes from `KEEP` plus the delay, not from the delay
alone.** A local edit enqueues a run five seconds out; edits arriving inside that
window hit `KEEP` against a request still sitting in `ENQUEUED` and are dropped,
so starring three files in a row produces one sync rather than three. The
foreground and manual triggers pass no delay, since someone is waiting on them.

Foreground is observed on `ProcessLifecycleOwner` rather than
`Activity.onStart`, so it fires once per app foreground — a rotation or a second
Activity does not re-trigger it. It also covers cold start, which is why
`MainActivity` schedules nothing of its own.

### The tail check

`KEEP` has one hole: an edit made while a sync is already **running** has its
trigger dropped, and nothing else would come along to pick it up. So at the end
of a pass the worker counts rows still in `LOCAL_ONLY`, `PENDING_UPDATE` or
`PENDING_PURGE` and re-enqueues if any remain.

Two details keep that from looping forever:

- `FAILED` is **excluded from the count**. A completed pass leaves every row it
  touched at `SYNCED` or `FAILED`, so counting `FAILED` would make a permanently
  rejected file — an oversized upload, say — re-enqueue a sync forever. What is
  left in the count is genuinely work that arrived mid-pass.
- The re-enqueue uses `APPEND_OR_REPLACE`, not `KEEP`. A running worker still
  counts as pending work under its own unique name, so a `KEEP` enqueue from
  inside it would be silently dropped and the follow-up would never happen.

## Conflict handling

Every mutation carries the last known `version`, and the server rejects a stale
one with **409**. The response body is not just an error — it contains the
server's current state of that file, which is enough to reconcile without a
re-fetch.

On a 409 from `PATCH`, `SyncRepo.pushUpdate` compares timestamps:

- If the server's `updatedAt` is **strictly newer**, the server wins: the local
  row is overwritten with the server state and marked `SYNCED`.
- Otherwise the local edit is re-sent once, at the version the server just
  reported.

The retry is bounded to a single attempt by a `retryOnConflict` flag, so a file
that keeps conflicting cannot spin. A second conflict means something else is
writing concurrently, and retrying again would not help — the server's state is
taken instead. Purges follow the same shape: one retry at the reported version,
then the row is left `FAILED` for the next delta pull to refresh.

Room migration 2→3 added the `version` column defaulting to 0, which is stale for
anything already uploaded. That is deliberate and self-healing: the first `PATCH`
on such a file returns 409 and the reconciliation above fixes it.

## Auth

Firebase handles sign-in (Google, via Credential Manager). The backend is told
nothing about identity beyond a token — **no request carries a UID**; the server
derives it from the verified token.

- `AuthInterceptor` attaches `Authorization: Bearer <Firebase ID token>` to every
  request. The fetch is blocking, which is safe because interceptors run on
  OkHttp's own threads; Firebase serves the token from memory unless it is near
  expiry. If there is no signed-in user the request goes out unauthenticated and
  surfaces as a 401, rather than throwing an `IOException` that callers would
  misreport as a transport failure.
- `TokenAuthenticator` handles the 401 by force-refreshing the token and
  replaying the request **once**. It matters most for `SyncWorker`, which can run
  long enough for a token minted at the start of a pass to expire mid-run. It
  gives up if a prior response exists, and also if the refresh returns the same
  token it just used — replaying that would only earn another 401.

## Setup

A fresh clone needs three things.

**1. A running backend.** Follow the setup in the
[backend README](https://github.com/pranavgoyal0102/DataNest_Backend). Nothing in
the app works without it; the UI will function offline but every sync will fail.

**2. `BASE_URL`.** Hardcoded in `data/remote/RetrofitInstance.kt`:

```kotlin
private const val BASE_URL = "http://172.16.39.122:8080/"
```

Point it at your own machine. For a physical device this must be your LAN IP, not
`localhost`; for the standard emulator, `http://10.0.2.2:8080/`. Cleartext HTTP is
permitted globally by `res/xml/network_security_config.xml` so that plain-HTTP
local development works — that config should not survive to production.

**3. Firebase.** `app/google-services.json` **is checked into this repository**,
so a fresh clone builds and signs in against the original Firebase project
without any extra step. To point at your own project you need to replace that
file *and* register an Android app under `applicationId`
`com.example.myapplication` with your debug signing SHA-1 — see the limitation
below on why that id has not been renamed.

Then `./gradlew assembleDebug`, or open the project in Android Studio.

## Known limitations

These are known and, where noted, deliberate.

**Permanent deletes do not propagate to offline devices.** The delta feed carries
no tombstones. A row with `isDeleted = true` is trashed rather than purged and
stays in the feed, so trashing syncs fine — but a file purged server-side simply
stops appearing, which is indistinguishable from it being unchanged. A device
offline across a purge therefore keeps its copy indefinitely. Closing this
requires the server to emit tombstones; the client cannot fix it alone.

**Uploads are capped at 100MB**, the ceiling on Cloudinary's free tier. The client
pre-flights against the same figure so an oversized file fails immediately
instead of after pushing the whole body, but the server's 413 remains
authoritative if the two ever drift apart.

**`BASE_URL` is a hardcoded LAN IP.** No build flavours, no
`local.properties` override. Every developer edits the same constant, and it must
not be committed pointing anywhere real.

**Uploads are not resumable.** A transfer interrupted at 99% restarts from zero
on the next sync. The row stays `FAILED` and is retried whole.

**There are no tests.** `ExampleUnitTest` and `ExampleInstrumentedTest` are the
generated stubs and assert `2 + 2 == 4`. The sync reconciliation logic in
particular — conflict resolution, cursor advancement, the tail check — is
verified by reading rather than by anything automated.

**`applicationId` is still `com.example.myapplication`** while the namespace and
package are `com.pranavgoyal.datanest`. It is the identity Firebase keys on, and
`google-services.json` registers only that one; renaming it means registering a
new Android app in the Firebase console with the signing SHA-1 first, and
invalidating existing installs.

**Several UI gaps are open:**

- Sharing **multiple** files into the app crashes. `MainNavigation` navigates to a
  `review_multiple_screen` route that is never defined, and the manifest declares
  an `ACTION_SEND_MULTIPLE` filter, so it is reachable from any gallery.
- **Opening and sharing a file work on opposite halves of the library.** Locally
  added files are stored as `content://` URIs and synced-down files as filesystem
  paths; the open path assumes a path and the share path assumes a URI, so each
  fails for the half the other handles.
- **Three controls do nothing**: the grid/list toggle swaps its icon but the list
  is always a `LazyColumn`; the "Download" row in the file menu has no click
  handler; the "Create" button in the folder dialog has an empty body. Folders
  are half-built more broadly — the entity and DAO exist, but `folderId` appears
  in no query.
- Sync failures are not surfaced. A failed row shows a small red icon with no
  explanation, including the size-limit message the client already generates.
