package com.pranavgoyal.datanest.data.remote.dto


data class SyncPageResponse(

    val changes: List<FileResponse>?,

    val cursor: String?,

    val hasMore: Boolean
)
