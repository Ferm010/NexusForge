package com.example.nexusforge.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Модель избранного проекта для Firestore
 */
data class FavoriteProject(
    @DocumentId
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String? = null,
    val author: String = "",
    val downloads: Int = 0,
    val categories: List<String> = emptyList(),
    val versions: List<String> = emptyList(),
    val projectType: String = "", // "mod" или "modpack"
    val addedAt: Timestamp = Timestamp.now()
) {
    companion object {
        /**
         * Конвертация из ModrinthProject в FavoriteProject
         */
        fun fromModrinthProject(project: ModrinthProject): FavoriteProject {
            return FavoriteProject(
                projectId = project.projectId,
                title = project.title,
                description = project.description,
                iconUrl = project.iconUrl,
                author = project.author,
                downloads = project.downloads,
                categories = project.categories,
                versions = project.versions,
                projectType = project.projectType,
                addedAt = Timestamp.now()
            )
        }
    }
}

/**
 * Ссылка на мод в сборке
 */
data class ModReference(
    val projectId: String = "",
    val title: String = "",
    val iconUrl: String? = null,
    val required: Boolean = true // обязательный или опциональный мод
)

/**
 * Пользовательская сборка модов
 */
data class CustomModpack(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val minecraftVersion: String = "",
    val modLoader: String = "", // "forge", "fabric", "quilt", "neoforge"
    val mods: List<ModReference> = emptyList(),
    val iconUrl: String? = null
)
