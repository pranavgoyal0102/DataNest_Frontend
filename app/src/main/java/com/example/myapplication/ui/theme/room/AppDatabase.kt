package com.example.myapplication.ui.theme.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "datanest_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
