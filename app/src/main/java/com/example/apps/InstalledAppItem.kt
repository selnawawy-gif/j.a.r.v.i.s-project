package com.example.apps

data class InstalledAppItem(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean = false
)
