package com.example.myapplication.ui.theme.room

import android.content.Context

object RepoProvider {
    private var INSTANCE: RoomRepo? = null

    fun getRepo(context: Context): RoomRepo {
        return INSTANCE ?: synchronized(this) {
            val db = DatabaseProvider.getDatabase(context)
            val repo = RoomRepo(db.fileDao(), db.folderDao())
            INSTANCE = repo
            repo
        }
    }
}
