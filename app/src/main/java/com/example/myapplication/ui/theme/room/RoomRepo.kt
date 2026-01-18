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
}
