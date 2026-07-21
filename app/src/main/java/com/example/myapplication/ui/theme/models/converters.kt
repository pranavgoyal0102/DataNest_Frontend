package com.example.myapplication.ui.theme.models


import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus): String {
        return syncStatus.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}