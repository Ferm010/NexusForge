package com.ferm.nexusforge.network

import com.ferm.nexusforge.data.GameVersion
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.data.ModrinthVersion
import com.ferm.nexusforge.data.ModrinthSearchResponse
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://api.modrinth.com/v2/"

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "NexusForge/1.0.0")
            .build()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()

interface ModrinthApiService {
    @GET("search")
    suspend fun searchProjects(
        @Query("query") query: String,
        @Query("facets") facets: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("index") index: String = "relevance"
    ): ModrinthSearchResponse
    
    @GET("tag/game_version")
    suspend fun getGameVersions(): List<GameVersion>
    
    @GET("projects")
    suspend fun getProjects(
        @Query("ids") ids: String
    ): List<ModrinthProject>
    
    @GET("project/{id}/version")
    suspend fun getProjectVersions(
        @retrofit2.http.Path("id") projectId: String
    ): List<ModrinthVersion>
    
    @GET("project/{id}")
    suspend fun getProject(
        @retrofit2.http.Path("id") projectId: String
    ): ModrinthProject
}

object ModrinthApi {
    val retrofitService: ModrinthApiService by lazy {
        retrofit.create(ModrinthApiService::class.java)
    }
}
