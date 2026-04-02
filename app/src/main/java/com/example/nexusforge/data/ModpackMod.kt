package com.example.nexusforge.data

data class ModpackMod(
    val projectId: String,
    val name: String,
    val version: String,
    val downloadUrl: String,
    val iconUrl: String?,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val sha1: String? = null,
    val sha512: String? = null
)