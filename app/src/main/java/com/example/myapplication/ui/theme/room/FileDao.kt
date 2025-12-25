package com.example.myapplication.ui.theme.room

import com.example.myapplication.ui.theme.models.FileStored
import androidx.room.*
import com.example.myapplication.ui.theme.mod.FolderEntity

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveFile(file: FileStored): Long

    @Delete
    suspend fun deleteFile(file: FileStored)

    @Query("SELECT * FROM files WHERE folderId = :folderId")
    suspend fun getFiles(folderId: Long): List<FileStored>

    @Query("SELECT * FROM files WHERE folderId = :folderId AND title LIKE '%' || :query || '%'")
    suspend fun searchFiles(query: String, folderId: Long): List<FileStored>

    @Query("SELECT * FROM files WHERE folderId = :folderId AND title = :name LIMIT 1")
    suspend fun getFileByName(folderId: Long, name: String): FileStored?

    @Update
    suspend fun updateFile(file: FileStored)
}
