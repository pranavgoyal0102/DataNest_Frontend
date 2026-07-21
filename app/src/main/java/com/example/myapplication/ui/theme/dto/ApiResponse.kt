package com.example.myapplication.ui.theme.dto

data class ApiResponse<T>(

    val success: Boolean,

    val message: String,

    val data: T?
)