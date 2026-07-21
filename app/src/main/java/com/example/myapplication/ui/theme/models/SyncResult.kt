package com.example.myapplication.ui.theme.models

sealed class SyncResult {

    data class Success(
        val remoteId: String
    ) : SyncResult()

    data class Error(
        val message: String
    ) : SyncResult()
}