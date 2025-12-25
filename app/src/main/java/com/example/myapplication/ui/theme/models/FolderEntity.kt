package com.example.myapplication.ui.theme.mod

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folder")
data class FolderEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val folderName : String,
    var parentId: Long? = null,
    val createdAt : Long = System.currentTimeMillis(),
    var isStarred : Boolean = false,
    var isDeleted : Boolean = false
)