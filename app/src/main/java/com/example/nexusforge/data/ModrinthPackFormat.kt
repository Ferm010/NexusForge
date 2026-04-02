package com.example.nexusforge.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modrinth Pack Format
 * Спецификация: https://docs.modrinth.com/docs/modpacks/format/
 */

@Serializable
data class ModrinthIndex(
    @SerialName("formatVersion")
    val formatVersion: Int = 1,
    val game: String = "minecraft",
    @SerialName("versionId")
    val versionId: String,
    val name: String,
    val summary: String? = null,
    val files: List<ModrinthFile>,
    val dependencies: ModrinthDependencies
)

@Serializable
data class ModrinthFile(
    val path: String,
    val hashes: ModrinthHashes,
    val env: ModrinthEnv? = null,
    val downloads: List<String>,
    val fileSize: Long
)

@Serializable
data class ModrinthHashes(
    val sha1: String,
    val sha512: String
)

@Serializable
data class ModrinthEnv(
    val client: String = "required",
    val server: String = "optional"
)

@Serializable
data class ModrinthDependencies(
    val minecraft: String,
    val forge: String? = null,
    val fabric: String? = null,
    @SerialName("fabric-loader")
    val fabricLoader: String? = null,
    val quilt: String? = null,
    @SerialName("quilt-loader")
    val quiltLoader: String? = null,
    val neoforge: String? = null
)
