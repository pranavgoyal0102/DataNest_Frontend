package com.pranavgoyal.datanest.ui.theme.dto

data class UpdateFileRequest(

    val title: String,

    val isDeleted: Boolean,

    val isStarred: Boolean,

    val version: Long

)