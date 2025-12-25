package com.example.myapplication.ui.theme.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileStored(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long,
    val size: Long? = 0,
    val date: Long = System.currentTimeMillis(),
    val title: String,
    val fileType: String,
    val path: String,
    var isStarred : Boolean = false,
    var isDeleted : Boolean = false
)
