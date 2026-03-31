package com.example.nexusforge.data

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
    val projectId: String,
    val slug: String,
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
    val license: String,
    val gallery: List<String>? = null
)

@Serializable
data class GameVersion(
    val version: String,
    @SerialName("version_type")
    val versionType: String,
    val date: String,
    val major: Boolean
)
