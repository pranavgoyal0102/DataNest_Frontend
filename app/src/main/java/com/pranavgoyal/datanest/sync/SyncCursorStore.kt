package com.pranavgoyal.datanest.sync

import android.content.Context

/**
 * Holds the delta-sync cursor across runs, in the same prefs file
 * [SyncScheduler] uses.
 *
 * The cursor is whatever the server last handed back, kept verbatim. It
 * is opaque: never parsed, ordered or reconstructed here. Null is the
 * never-synced state, and asks the server for everything.
 */
class SyncCursorStore(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

    /**
     * Written with commit() rather than apply(): the caller persists this
     * only after a page is applied, and a background sync can be killed
     * the moment it returns. An apply() still in flight would lose the
     * page boundary and re-pull it next run.
     */
    var cursor: String?
        get() =
            prefs.getString(
                KEY_CURSOR,
                null
            )
        set(value) {

            val editor = prefs.edit()

            if (value == null) {
                editor.remove(KEY_CURSOR)
            } else {
                editor.putString(
                    KEY_CURSOR,
                    value
                )
            }

            editor.commit()
        }

    /** Drops the cursor, so the next sync pulls the whole account. */
    fun clear() {
        prefs.edit()
            .remove(KEY_CURSOR)
            .remove(KEY_CURSOR_LEGACY)
            .commit()
    }

    private companion object {

        const val PREF_NAME = "sync_prefs"

        const val KEY_CURSOR = "delta_cursor_v2"

        /**
         * Held a Long back when the cursor was thought to be a
         * timestamp. Reading that key as a String throws
         * ClassCastException, so the key moved rather than the type —
         * a device upgrading from that build starts from no cursor
         * instead of crashing.
         */
        const val KEY_CURSOR_LEGACY = "delta_cursor"
    }
}
