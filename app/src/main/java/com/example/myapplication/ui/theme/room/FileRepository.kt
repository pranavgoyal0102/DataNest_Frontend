package com.example.myapplication.ui.theme.room

import android.util.Log
import com.example.myapplication.ui.theme.models.FileStored
import kotlinx.coroutines.flow.Flow

class FileRepository(
    private val fileDao: FileDao
) {

    suspend fun getPendingUploadFiles() =
        fileDao.getPendingUploadFiles()

    suspend fun saveFile(
        file: FileStored
    ): Long {

        Log.e(
            "FILE_SOURCE",
            "Saving ${file.title}",
            Exception("TRACE")
        )

        Log.d(
            "ROOM",
            "Saving ${file.title}"
        )

        return fileDao.insertFile(file)
    }

    fun getFiles(): Flow<List<FileStored>> {
        return fileDao.getFiles()
    }

    fun searchFiles(query: String): Flow<List<FileStored>> {
        return fileDao.searchFiles(query)
    }

    fun getDeletedFiles(): Flow<List<FileStored>> {
        return fileDao.getDeletedFiles()
    }

    suspend fun getFileById(id: Long): FileStored? {
        return fileDao.getFileById(id)
    }

    suspend fun getFileByName(name: String): FileStored? {
        return fileDao.getFileByName(name)
    }


    suspend fun updateFile(file: FileStored) {
        fileDao.updateFile(file)
    }

    suspend fun updateSyncStatus(
        id: Long,
        status: String
    ) {

        Log.d(
            "SYNC",
            "File $id -> $status"
        )

        fileDao.updateSyncStatus(
            id,
            status.toString()
        )
    }

    suspend fun getFilesCount(): Int {
        return fileDao.getFilesCount()
    }

    suspend fun getFileByRemoteId(remoteId: String): FileStored? =
        fileDao.getFileByRemoteId(remoteId)

    suspend fun getLocalOnlyFiles(): List<FileStored> {
        return fileDao.getLocalOnlyFiles()
    }


    suspend fun getPendingUpdateFiles(): List<FileStored> {
        return fileDao.getPendingUpdateFiles()
    }


    suspend fun getPendingDeleteFiles(): List<FileStored> {
        return fileDao.getPendingDeleteFiles()
    }


    suspend fun updateRemoteId(
        id: Long,
        remoteId: String
    ) {
        fileDao.updateRemoteId(
            id,
            remoteId
        )
    }

    suspend fun deleteFile(
        file: FileStored
    ) {
        fileDao.deleteFile(file)
    }


}