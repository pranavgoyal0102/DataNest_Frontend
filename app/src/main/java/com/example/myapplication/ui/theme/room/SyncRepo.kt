package com.example.myapplication.ui.theme.room

import android.util.Log

class SyncRepo(
    private val fileDao : FileDao,
    private val remote : FileRemoteDataSource
) {

    suspend fun sync(){
        updatePending()
        download()
    }

    suspend fun updatePending() {

        val files = fileDao.getUnSyncedFiles()

        for (file in files) {
            try {
                fileDao.updateTime(file.id,System.currentTimeMillis())
                fileDao.markAsSynced(file.id)
                remote.upload(file)
            } catch (e: Exception) {

            }
        }
    }


    private suspend fun download() {
        val remoteFiles = remote.fetchAll()

        Log.d("SYNC_FLOW", "download started")
        Log.d("SYNC_FLOW", "remote files count = ${remoteFiles.size}")

        for (remoteFile in remoteFiles) {

            Log.d("SYNC_FLOW", "processing ${remoteFile.syncId}")

            val local = fileDao.getFilesBySyncId(remoteFile.syncId)

            Log.d("SYNC_FLOW", "local = $local")

            if (local == null) {
                Log.d("SYNC_FLOW", "INSERT ${remoteFile.title}")
                fileDao.saveFile(remoteFile.copy(isSynced = true, isDeleted = false))
            } else if (remoteFile.updatedAt > local.updatedAt) {
                Log.d("SYNC_FLOW", "UPDATE ${remoteFile.title}")
                fileDao.updateFile(
                    remoteFile.copy(id = local.id, isSynced = true)
                )
            }
        }

        Log.d("SYNC_FLOW", "download finished")
    }

}