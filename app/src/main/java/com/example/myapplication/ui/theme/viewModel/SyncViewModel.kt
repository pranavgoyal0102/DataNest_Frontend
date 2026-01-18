package com.example.myapplication.ui.theme.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.room.AppDatabase
import com.example.myapplication.ui.theme.room.FileRemoteDataSource
import com.example.myapplication.ui.theme.room.SyncRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SyncRepo(
        database.fileDao(),
        FileRemoteDataSource()
    )

    fun syncNow() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.sync()
            } catch (e: Exception) {
                Log.e("SYNC_FLOW", "Sync failed", e)
            }
        }
    }

}
