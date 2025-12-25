package com.example.myapplication.ui.theme.room

import com.example.myapplication.ui.theme.mod.FolderEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createFolder(folder : FolderEntity) : Long

    @Query("SELECT * FROM folder WHERE parentId is -1")
    suspend fun getRootFolders() : FolderEntity?

    @Query("SELECT * FROM folder WHERE parentId = :parentId AND folderName = :name LIMIT 1")
    suspend fun getFolderByName(parentId: Long, name: String): FolderEntity?

    @Query("SELECT * FROM folder WHERE parentId = :parentId")
    suspend fun getChildFolders(parentId : Long) : List<FolderEntity>

    @Query("SELECT * FROM folder WHERE parentId = :parentId AND folderName LIKE '%' || :query || '%'")
    suspend fun searchFolders(query: String, parentId: Long): List<FolderEntity>

    @Query("SELECT * FROM folder WHERE id = :id")
    suspend fun getFolderById(id : Long) : FolderEntity

    @Delete
    suspend fun deleteFolder(folder : FolderEntity)

    @Update
    suspend fun updateFolder(folder: FolderEntity)
}