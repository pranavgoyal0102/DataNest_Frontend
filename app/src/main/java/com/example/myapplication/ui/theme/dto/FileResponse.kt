package com.example.myapplication.ui.theme.dto

data class FileResponse(

    val id: String,

    val title: String,

    val mimeType: String,

    val size: Long,

    val cloudUrl: String,

    val isStarred: Boolean,

    val isDeleted: Boolean,

    val createdAt: Long,

    val updatedAt: Long,

    val version: Long
)