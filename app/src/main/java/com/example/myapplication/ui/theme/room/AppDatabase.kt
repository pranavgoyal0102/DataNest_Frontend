package com.example.myapplication.ui.theme.room


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.FileStored

@Database(
    entities = [FileStored::class, FolderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun folderDao(): FolderDao
}

