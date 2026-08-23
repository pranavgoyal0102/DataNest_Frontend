package com.pranavgoyal.datanest.data.remote.dto

/**
 * One page of GET api/files/sync, carried under the usual [ApiResponse]
 * envelope.
 *
 * [changes] is an ordinary list of rows, tombstones included in neither
 * form nor spirit — a row with isDeleted = true is trashed, not purged,
 * and is applied like any other. A file purged server-side simply stops
 * appearing here, so the delta carries no signal to remove it locally.
 */
data class SyncPageResponse(

    /**
     * Nullable on purpose. Gson populates these by reflection and will
     * happily leave a non-null Kotlin field null when the JSON does not
     * match, so declaring it non-null buys nothing but an NPE at the
     * first use. Callers check.
     */
    val changes: List<FileResponse>?,

    /**
     * Cursor for the next page. Opaque — it is stored and sent back
     * verbatim, never parsed, ordered or rebuilt client-side. Null only
     * if the payload is malformed.
     */
    val cursor: String?,

    val hasMore: Boolean
)
