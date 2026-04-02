package com.example.nexusforge.data

data class ModpackTemplate(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val mods: List<TemplateMod> = emptyList(),
    val minecraftVersion: String = "",
    val modLoader: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
)

data class TemplateMod(
    val projectId: String = "",
    val name: String = "",
    val iconUrl: String? = null
)
