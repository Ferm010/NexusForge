package com.example.nexusforge.repository

import com.example.nexusforge.data.CustomModpack
import com.example.nexusforge.data.FavoriteProject
import com.example.nexusforge.data.ModpackTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository для работы с Firestore
 * Управляет избранными проектами и пользовательскими сборками
 */
class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private fun getUserId(): String? = auth.currentUser?.uid
    
    // ==================== ИЗБРАННОЕ ====================
    
    /**
     * Получить все избранные проекты пользователя в реальном времени
     */
    fun getFavorites(): Flow<List<FavoriteProject>> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FavoriteProject::class.java)
                } ?: emptyList()
                
                trySend(favorites)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Добавить проект в избранное
     */
    suspend fun addToFavorites(project: FavoriteProject): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(project.projectId)
                .set(project)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Удалить проект из избранного
     */
    suspend fun removeFromFavorites(projectId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(projectId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Проверить, находится ли проект в избранном
     */
    suspend fun isFavorite(projectId: String): Boolean {
        return try {
            val userId = getUserId() ?: return false
            
            val doc = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(projectId)
                .get()
                .await()
            
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    // ==================== ПОЛЬЗОВАТЕЛЬСКИЕ СБОРКИ ====================
    
    /**
     * Получить все пользовательские сборки в реальном времени
     */
    fun getCustomModpacks(): Flow<List<CustomModpack>> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("custom_modpacks")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val modpacks = snapshot?.documents?.mapNotNull { doc ->
                    val modpack = doc.toObject(CustomModpack::class.java)
                    modpack?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(modpacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Получить избранные пользовательские сборки
     */
    fun getFavoriteModpacks(): Flow<List<CustomModpack>> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("custom_modpacks")
            .whereEqualTo("isFavorite", true)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val modpacks = snapshot?.documents?.mapNotNull { doc ->
                    val modpack = doc.toObject(CustomModpack::class.java)
                    modpack?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(modpacks)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Создать новую пользовательскую сборку
     */
    suspend fun createCustomModpack(modpack: CustomModpack): Result<String> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document()
            
            val modpackWithId = modpack.copy(id = docRef.id)
            docRef.set(modpackWithId).await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Обновить существующую сборку
     */
    suspend fun updateCustomModpack(modpack: CustomModpack): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpack.id)
                .set(modpack)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Удалить пользовательскую сборку
     */
    suspend fun deleteCustomModpack(modpackId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить конкретную сборку по ID
     */
    suspend fun getCustomModpack(modpackId: String): Result<CustomModpack?> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val doc = firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .get()
                .await()
            
            val modpack = doc.toObject(CustomModpack::class.java)
                ?.copy(id = doc.id)
            Result.success(modpack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Сохранить пользовательскую сборку (для конструктора)
     */
    suspend fun saveCustomModpack(modpackId: String, modpackData: Map<String, Any>): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .set(modpackData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== ШАБЛОНЫ ====================
    
    /**
     * Получить все шаблоны пользователя
     */
    fun getTemplates(): Flow<List<ModpackTemplate>> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("templates")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val templates = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ModpackTemplate::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(templates)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Сохранить шаблон
     */
    suspend fun saveTemplate(template: ModpackTemplate): Result<String> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val templateData = template.copy(userId = userId)
            val docRef = if (template.id.isEmpty()) {
                firestore.collection("users")
                    .document(userId)
                    .collection("templates")
                    .document()
            } else {
                firestore.collection("users")
                    .document(userId)
                    .collection("templates")
                    .document(template.id)
            }
            
            docRef.set(templateData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получить шаблон по ID
     */
    suspend fun getTemplate(templateId: String): Result<ModpackTemplate?> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val doc = firestore.collection("users")
                .document(userId)
                .collection("templates")
                .document(templateId)
                .get()
                .await()
            
            val template = doc.toObject(ModpackTemplate::class.java)?.copy(id = doc.id)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Удалить шаблон
     */
    suspend fun deleteTemplate(templateId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            firestore.collection("users")
                .document(userId)
                .collection("templates")
                .document(templateId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

