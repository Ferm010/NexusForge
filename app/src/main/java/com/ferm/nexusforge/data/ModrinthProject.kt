package com.ferm.nexusforge.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthSearchResponse(
    val hits: List<ModrinthProject>,
    val offset: Int,
    val limit: Int,
    @SerialName("total_hits")
    val totalHits: Int
)

@Serializable
data class ModrinthProject(
    @SerialName("project_id")
    val projectId: String = "",
    val id: String = "",
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val categories: List<String> = emptyList(),
    @SerialName("client_side")
    val clientSide: String = "",
    @SerialName("server_side")
    val serverSide: String = "",
    @SerialName("project_type")
    val projectType: String = "",
    val downloads: Int = 0,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val author: String = "",
    val team: String = "",
    val versions: List<String> = emptyList(),
    val follows: Int = 0,
    @SerialName("date_created")
    val dateCreated: String = "",
    @SerialName("date_modified")
    val dateModified: String = ""
) {
    val actualProjectId: String
        get() = if (projectId.isNotEmpty()) projectId else id
}

@Serializable
data class GameVersion(
    val version: String,
    @SerialName("version_type")
    val versionType: String,
    val date: String,
    val major: Boolean
)

@Serializable
data class ModrinthProjectDetails(
    val id: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @SerialName("client_side")
    val clientSide: String,
    @SerialName("server_side")
    val serverSide: String,
    @SerialName("project_type")
    val projectType: String,
    val downloads: Int,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val author: String,
    val versions: List<String>,
    val follows: Int,
    @SerialName("date_created")
    val dateCreated: String,
    @SerialName("date_modified")
    val dateModified: String,
    @SerialName("download_url")
    val downloadUrl: String? = null,
    val modrinth_details_url: String? = null
)

@Serializable
data class ModrinthVersion(
    val id: String,
    @SerialName("project_id")
    val projectId: String = "",
    val name: String = "",
    @SerialName("version_number")
    val versionNumber: String = "",
    @SerialName("game_version")
    val gameVersion: List<String> = emptyList(),
    @SerialName("version_type")
    val versionType: String = "",
    val loaders: List<String> = emptyList(),
    val downloads: Int = 0,
    @SerialName("download_url")
    val downloadUrl: String? = null,
    val files: List<ModFile> = emptyList(),
    @SerialName("primary_file")
    val primaryFile: String? = null,
    @SerialName("game_versions")
    val gameVersions: List<String>? = null
)

@Serializable
data class ModFile(
    val filename: String = "",
    val size: Long = 0,
    val url: String? = null,
    @SerialName("primary")
    val primary: Boolean? = null,
    val hashes: Map<String, String>? = null
)
