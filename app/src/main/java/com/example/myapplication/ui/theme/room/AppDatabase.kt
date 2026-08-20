package com.example.myapplication.ui.theme.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.Converters
import com.example.myapplication.ui.theme.models.FileStored

@Database(
    entities = [FileStored::class, FolderEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fileDao(): FileDao
    abstract fun folderDao(): FolderDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Adds the server-assigned version column. Existing rows get 0,
         * which is stale for anything already uploaded — the first PATCH
         * on such a file returns 409 and SyncRepo reconciles from the
         * server state in that response.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE files
                    ADD COLUMN version INTEGER NOT NULL DEFAULT 0
                    """
                )
            }
        }

        /**
         * Renames the PENDING_DELETE sync status to PENDING_PURGE. Not
         * cosmetic — SyncStatus is persisted by name and read back
         * through SyncStatus.valueOf, so any row left holding the old
         * string would throw the moment it is loaded.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    UPDATE files
                    SET syncStatus = 'PENDING_PURGE'
                    WHERE syncStatus = 'PENDING_DELETE'
                    """
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "datanest_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}