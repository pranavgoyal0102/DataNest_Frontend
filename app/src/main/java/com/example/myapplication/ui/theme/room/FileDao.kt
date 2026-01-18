package com.example.myapplication.ui.theme.room

import com.example.myapplication.ui.theme.models.FileStored
import androidx.room.*
import com.example.myapplication.ui.theme.mod.FolderEntity

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFile(file: FileStored): Long

    @Delete
    suspend fun deleteFile(file: FileStored)

    @Query("SELECT * FROM files WHERE folderId = :folderId")
    suspend fun getFiles(folderId: Long): List<FileStored>

    @Query("SELECT * FROM files WHERE folderId = :folderId AND title LIKE '%' || :query || '%'")
    suspend fun searchFiles(query: String, folderId: Long): List<FileStored>

    @Query("SELECT * FROM files WHERE folderId = :folderId AND title = :name LIMIT 1")
    suspend fun getFileByName(folderId: Long, name: String): FileStored?

    @Query("SELECT * FROM files WHERE syncId = :syncId LIMIT 1")
    suspend fun getFilesBySyncId(syncId: String): FileStored?

    @Query("SELECT * FROM files WHERE isSynced = 0")
    suspend fun getUnSyncedFiles() : List<FileStored>

    @Query("UPDATE files set isSynced = 1 where id = :id")
    suspend fun markAsSynced(id : Long)

    @Query("UPDATE files set updatedAt = :time where id = :id")
    suspend fun updateTime(id : Long, time: Long)

    @Update
    suspend fun updateFile(file: FileStored)

    @Query("UPDATE files SET isSynced = 0")
    suspend fun markAllAsUnsynced()

}
