package com.pranavgoyal.datanest.ui.viewmodel


import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pranavgoyal.datanest.data.local.FileStored
import com.pranavgoyal.datanest.data.local.SyncStatus
import com.pranavgoyal.datanest.data.remote.FileRemoteRepository
import kotlinx.coroutines.launch
import com.pranavgoyal.datanest.data.local.AppDatabase
import com.pranavgoyal.datanest.data.local.FileRepository
import com.pranavgoyal.datanest.sync.SyncRepo
import com.pranavgoyal.datanest.sync.SyncScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest


class RoomViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val repository = FileRepository(
        db.fileDao()
    )
    private val remoteRepository =
        FileRemoteRepository()

    private val syncRepository =
        SyncRepo(
            application,
            repository,
            remoteRepository
        )
    private var searchJob: Job? = null

    val fileList = repository
        .getFiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedFiles =
        repository
            .getDeletedFiles()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _searchFileList =
        MutableStateFlow<List<FileStored>>(emptyList())

    val searchFileList: StateFlow<List<FileStored>>
        get() = _searchFileList.asStateFlow()


    fun saveFile(
        file: FileStored,
        context: Context
    ) {

        viewModelScope.launch {

            repository.saveFile(file)

            SyncScheduler.start(
                context
            )
        }
    }

    /**
     * Explicit "delete permanently" from the trash screen. Use
     * [moveToTrash] for an ordinary delete.
     */
    fun purgeFile(file: FileStored) {
        viewModelScope.launch {

            // Re-read rather than writing back the row the UI is
            // holding: that copy predates anything a sync in flight
            // wrote, so persisting it would roll the version — and a
            // just-synced trash — back to a stale value.
            val current =
                repository.getFileById(file.id)
                    ?: return@launch

            repository.updateFile(

                current.copy(

                    updatedAt =
                    System.currentTimeMillis(),

                    syncStatus =
                    SyncStatus.PENDING_PURGE
                )
            )
        }
    }

    fun search(query: String) {

        searchJob?.cancel()

        searchJob = viewModelScope.launch {

            if (query.isBlank()) {

                repository
                    .getFiles()
                    .collectLatest {

                        _searchFileList.value = it

                    }

            } else {

                repository
                    .searchFiles(query)
                    .collectLatest {

                        _searchFileList.value = it

                    }
            }
        }
    }

    fun updateFile(
        file: FileStored,
        onResult: (Boolean, String) -> Unit
    ) {

        viewModelScope.launch {

            val existing =
                repository.getFileByName(file.title)

            if (
                existing != null &&
                existing.id != file.id
            ) {

                onResult(
                    false,
                    "File with this name already exists!"
                )

            } else {

                repository.updateFile(
                    file.copy(
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )

                onResult(
                    true,
                    "File Updated"
                )
            }
        }
    }


    fun moveToTrash(id: Long) {

        viewModelScope.launch {

            val file =
                repository.getFileById(id)

            if (file != null) {

                repository.updateFile(

                    file.copy(

                        isDeleted = true,

                        updatedAt =
                        System.currentTimeMillis(),

                        syncStatus =
                        SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }
    }

    fun restoreFromTrash(id: Long) {

        viewModelScope.launch {

            val file =
                repository.getFileById(id)

            if (file != null) {

                repository.updateFile(

                    file.copy(

                        isDeleted = false,

                        updatedAt =
                        System.currentTimeMillis(),

                        syncStatus =
                        SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }
    }

    fun syncAllFiles() {

        FirebaseAuth
            .getInstance()
            .currentUser
            ?: return

        viewModelScope.launch {

            syncRepository.syncAllFiles()
        }
    }
}