package com.pranavgoyal.datanest.data.remote.dto

data class UpdateFileRequest(

    val title: String,

    val isDeleted: Boolean,

    val isStarred: Boolean,

    val version: Long

)