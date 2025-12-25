package com.example.myapplication.ui.theme.room

import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.FileStored

class RoomRepo(
    private val fileDao: FileDao,
    private val folderDao: FolderDao
) {
    suspend fun saveFile(item: FileStored) = fileDao.saveFile(item)
    suspend fun deleteFile(item: FileStored) = fileDao.deleteFile(item)
    suspend fun updateFile(item: FileStored) = fileDao.updateFile(item)
    suspend fun getFiles(id : Long) = fileDao.getFiles(id)
    suspend fun createFolder(item: FolderEntity) = folderDao.createFolder(item)
    suspend fun getRootFolder() = folderDao.getRootFolders()
    suspend fun deleteFolder(item : FolderEntity) = folderDao.deleteFolder(item)
    suspend fun getChildFolders(parentId : Long) = folderDao.getChildFolders(parentId)
    suspend fun updateFolder(item: FolderEntity) = folderDao.updateFolder(item)
}
