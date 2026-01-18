package com.example.myapplication.ui.theme.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.room.AppDatabase
import kotlinx.coroutines.launch

class RoomViewModel(application: Application) : AndroidViewModel(application)  {
    private var _folderList = MutableLiveData<List<FolderEntity>>(emptyList())
    val folderList : MutableLiveData<List<FolderEntity>> = _folderList
    private var _fileList = MutableLiveData<List<FileStored?>>(emptyList())
    val fileList = _fileList

    private var _searchFolderList = MutableLiveData<List<FolderEntity>>(emptyList())
    val searchFolderList : MutableLiveData<List<FolderEntity>> = _searchFolderList
    private var _searchFileList = MutableLiveData<List<FileStored?>>(emptyList())
    val searchFileList = _searchFileList

    private val db = AppDatabase.getDatabase(application)

    init{
        createHomeFolder()
    }

    fun getFiles(id : Long) {
        viewModelScope.launch {
            _fileList.value = db.fileDao().getFiles(id)
        }
    }
    fun saveFile(file : FileStored) {
        viewModelScope.launch {
            db.fileDao().saveFile(file)
            val folderId = file.folderId
            getFiles(folderId)
        }
    }
    fun search(query: String, folderId: Long) {
        viewModelScope.launch {
            val files = db.fileDao().searchFiles(query, folderId)
            _searchFileList.value = files

            val folders = db.folderDao().searchFolders(query, folderId)
            _searchFolderList.value = folders
        }
    }

    fun updateFile(file: FileStored, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val originalExtension = file.title.substringAfterLast('.', "")

            var safeTitle = file.title
            if (!file.title.contains(".") && originalExtension.isNotEmpty()) {
                safeTitle = "${file.title}.$originalExtension"
            }
            val safeFile = file.copy(title = safeTitle)
            val existing = db.folderDao().getFolderByName(safeFile.folderId, safeFile.title)
            if (existing != null) {
                onResult(false, "File with this name already exists!")
            } else {
                db.fileDao().updateFile(safeFile)
                onResult(true, "File Renamed")
                getFiles(safeFile.folderId)
            }
        }
    }

    fun deleteFile(file : FileStored) {
        viewModelScope.launch {
            db.fileDao().deleteFile(file)
            val folderId = file.folderId
            getFiles(folderId)
        }
    }
    fun updateFolder(folder: FolderEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val existing = db.folderDao().getFolderByName(folder.parentId ?: 0, folder.folderName)
                if (existing != null && existing.id != folder.id) {
                    onResult(false, "Folder with this name already exists!")
                } else {
                    db.folderDao().updateFolder(folder)
                    onResult(true, "Folder Updated")
                    getFolders(folder.parentId ?: 0)
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun getFolders(parentId : Long) {
        viewModelScope.launch {
            _folderList.value = db.folderDao().getChildFolders(parentId)
        }
    }
    fun saveFolder(folder: FolderEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = db.folderDao().getFolderByName(folder.parentId ?: 0, folder.folderName)
            if (existing != null) {
                onResult(false, "Folder with this name already exists!")
            } else {
                db.folderDao().createFolder(folder)
                onResult(true, "Folder Created")
                getFolders(folder.parentId ?: 0)
            }
        }
    }
    suspend fun getFolderById(id: Long): FolderEntity {
        return db.folderDao().getFolderById(id)
    }
    fun deleteFolder(folder : FolderEntity) {
        val pid = folder.parentId
        viewModelScope.launch {
            db.folderDao().deleteFolder(folder)
        }
        getFolders(pid ?: -1)
    }
    private fun createHomeFolder() {
        viewModelScope.launch {
            val home = db.folderDao().getRootFolders()
            val bin = db.folderDao().getRootFolders()
            if(home == null){
                val homeFolder = FolderEntity(folderName = "Home", parentId = -1)
                db.folderDao().createFolder(homeFolder)
            }
            if(bin == null){
                val binFolder = FolderEntity(folderName = "Bin", parentId = -1)
                db.folderDao().createFolder((binFolder))
            }
        }
    }
}