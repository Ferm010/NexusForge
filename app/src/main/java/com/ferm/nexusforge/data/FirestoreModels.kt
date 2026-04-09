package com.ferm.nexusforge.data

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
    val required: Boolean = true, // обязательный или опциональный мод
    val downloadUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val sha1: String? = null,
    val sha512: String? = null
)

/**
 * Пользовательская сборка модов
 */
data class CustomModpack(
    val id: String = "", // ID документа
    val name: String = "",
    val description: String = "",
    val createdAt: Any = Timestamp.now(),
    val updatedAt: Any = Timestamp.now(),
    val minecraftVersion: String = "",
    val modLoader: String = "",
    val mods: List<ModReference> = emptyList(),
    val iconUrl: String? = null,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)
