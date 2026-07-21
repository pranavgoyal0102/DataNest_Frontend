package com.example.myapplication.ui.theme.dto

data class CreateFileRequest(

    val firebaseUid: String,

    val title: String,

    val mimeType: String,

    val size: Long,

    val isDeleted: Boolean,

    val isStarred: Boolean
)