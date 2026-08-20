package com.pranavgoyal.datanest.ui.theme.dto

data class CreateFileRequest(

    val title: String,

    val mimeType: String,

    val size: Long,

    val isDeleted: Boolean,

    val isStarred: Boolean
)