package com.pranavgoyal.datanest.ui.theme.room

import androidx.room.*
import com.pranavgoyal.datanest.ui.theme.models.FileStored
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileStored): Long


    @Update
    suspend fun updateFile(file: FileStored)


    @Query("""
        SELECT * FROM files
        WHERE isDeleted = 0
        ORDER BY updatedAt DESC
    """)
    fun getFiles(): Flow<List<FileStored>>


    @Query("""
        SELECT * FROM files
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getFileById(id: Long): FileStored?



    @Query("""
        SELECT * FROM files
        WHERE title = :name
        LIMIT 1
    """)
    suspend fun getFileByName(name: String): FileStored?



    @Query("""
        SELECT * FROM files
        WHERE title LIKE '%' || :query || '%'
        AND isDeleted = 0
        ORDER BY updatedAt DESC
    """)
    fun searchFiles(query: String): Flow<List<FileStored>>


    @Query("""
        SELECT * FROM files
        WHERE isStarred = 1
        AND isDeleted = 0
        ORDER BY updatedAt DESC
    """)
    fun getStarredFiles(): Flow<List<FileStored>>


    @Query("""
        SELECT * FROM files
        WHERE isDeleted = 1
        ORDER BY updatedAt DESC
    """)
    fun getDeletedFiles(): Flow<List<FileStored>>


    @Query("""
        SELECT * FROM files
        WHERE folderId = :folderId
        AND isDeleted = 0
        ORDER BY title ASC
    """)
    fun getFilesByFolder(folderId: Long): Flow<List<FileStored>>



    @Query("""
        UPDATE files
        SET syncStatus = 'SYNCED'
        WHERE id = :id
    """)
    suspend fun markAsSynced(id: Long)


    @Query("""
        UPDATE files
        SET syncStatus = 'LOCAL_ONLY'
    """)
    suspend fun markAllAsUnsynced()


    @Query("""
        UPDATE files
        SET syncStatus = :status
        WHERE id = :id
    """)
    suspend fun updateSyncStatus(
        id: Long,
        status: String
    )


    @Query("""
    UPDATE files
    SET isStarred = :starred,
        updatedAt = :time
    WHERE id = :id
""")
    suspend fun updateStarStatus(
        id: Long,
        starred: Boolean,
        time: Long
    )


    @Query("""
    UPDATE files
    SET isDeleted = 1,
        updatedAt = :time
    WHERE id = :id
""")
    suspend fun moveToTrash(
        id: Long,
        time: Long
    )


    @Query("""
    UPDATE files
    SET isDeleted = 0,
        updatedAt = :time
    WHERE id = :id
""")
    suspend fun restoreFromTrash(
        id: Long,
        time: Long
    )



    @Query("""
        SELECT COUNT(*)
        FROM files
        WHERE isDeleted = 0
    """)
    suspend fun getFilesCount(): Int


    @Query("""
        SELECT COUNT(*)
        FROM files
        WHERE syncStatus != 'SYNCED'
    """)
    suspend fun getPendingSyncCount(): Int


    @Query("""
    SELECT EXISTS(
        SELECT 1
        FROM files
        WHERE title = :title
    )
""")
    suspend fun fileExists(
        title: String
    ): Boolean

    @Query("""
    SELECT COUNT(*)
    FROM files
    WHERE syncStatus = 'SYNCED'
""")
    suspend fun getSyncedCount(): Int

    @Query("SELECT * FROM files WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getFileByRemoteId(remoteId: String): FileStored?


    @Query("""
    SELECT * FROM files
    WHERE syncStatus = 'LOCAL_ONLY'
""")
    suspend fun getLocalOnlyFiles(): List<FileStored>


    @Query("""
    SELECT * FROM files
    WHERE syncStatus = 'PENDING_UPDATE'
""")
    suspend fun getPendingUpdateFiles(): List<FileStored>


    @Query("""
    SELECT * FROM files
    WHERE syncStatus = 'PENDING_PURGE'
""")
    suspend fun getPendingPurgeFiles(): List<FileStored>

    @Query("""
    UPDATE files
    SET remoteId = :remoteId,
        version = :version
    WHERE id = :id
""")
    suspend fun updateRemoteId(
        id: Long,
        remoteId: String,
        version: Long
    )

    @Query("""
    UPDATE files
    SET version = :version
    WHERE id = :id
""")
    suspend fun updateVersion(
        id: Long,
        version: Long
    )

    @Delete
    suspend fun deleteFile(
        file: FileStored
    )

    @Query("""
    SELECT * FROM files
    WHERE syncStatus = 'FAILED'
    """)
    suspend fun getFailedFiles(): List<FileStored>

    @Query("""
    SELECT * FROM files
    WHERE syncStatus = 'LOCAL_ONLY'
    OR syncStatus = 'FAILED'
    """)
    suspend fun getPendingUploadFiles(): List<FileStored>

}