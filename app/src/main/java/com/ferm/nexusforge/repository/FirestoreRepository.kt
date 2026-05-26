package com.ferm.nexusforge.repository

import android.util.Log
import com.ferm.nexusforge.data.CustomModpack
import com.ferm.nexusforge.data.FavoriteProject
import com.ferm.nexusforge.data.ModpackTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepository"

class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val activeListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
    
    private fun getUserId(): String? = auth.currentUser?.uid
    

    fun clearAllListeners() {
        Log.d(TAG, "Clearing ${activeListeners.size} active Firestore listeners")
        activeListeners.toList().forEach { listener ->
            try {
                listener.remove()
            } catch (e: Exception) {
                Log.w(TAG, "Error removing listener during sign out: ${e.message}")
            }
        }
        activeListeners.clear()
    }

    suspend fun createUserProfile(email: String, displayName: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val userData = mapOf(
                "email" to email,
                "displayName" to displayName,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()
            
            Log.d(TAG, "User profile created successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun checkUserProfileExists(): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val exists = doc.exists()
            Log.d(TAG, "User profile exists: $exists")
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        
        activeListeners.add(listener)
        awaitClose { 
            listener.remove()
            activeListeners.remove(listener)
        }
    }

    suspend fun addToFavorites(project: FavoriteProject): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Adding project to favorites")

            val userDocRef = firestore.collection("users").document(userId)
            val userDoc = userDocRef.get().await()
            
            if (!userDoc.exists()) {
                Log.d(TAG, "User document doesn't exist, creating it")
                val userData = mapOf(
                    "email" to (auth.currentUser?.email ?: ""),
                    "displayName" to (auth.currentUser?.displayName ?: ""),
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                userDocRef.set(userData).await()
            }
            
            val projectData = mapOf(
                "projectId" to project.projectId,
                "title" to project.title,
                "description" to project.description,
                "iconUrl" to project.iconUrl,
                "author" to project.author,
                "downloads" to project.downloads,
                "categories" to project.categories,
                "versions" to project.versions,
                "projectType" to project.projectType,
                "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(project.projectId)
                .set(projectData)
                .await()
            
            Log.d(TAG, "Project added to favorites successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun removeFromFavorites(projectId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Removing project from favorites")
            
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(projectId)
                .delete()
                .await()
            
            Log.d(TAG, "Project removed from favorites successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        
        activeListeners.add(listener)
        awaitClose { 
            listener.remove()
            activeListeners.remove(listener)
        }
    }
    

    suspend fun createCustomModpack(modpack: CustomModpack): Result<String> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Creating custom modpack")
            
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document()
            
            val modpackWithId = modpack.copy(id = docRef.id)
            docRef.set(modpackWithId).await()
            
            Log.d(TAG, "Custom modpack created successfully")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun updateCustomModpack(modpack: CustomModpack): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Updating custom modpack")
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpack.id)
                .set(modpack)
                .await()
            
            Log.d(TAG, "Custom modpack updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomModpack(modpackId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Deleting custom modpack")
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .delete()
                .await()
            
            Log.d(TAG, "Custom modpack deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun getCustomModpack(modpackId: String): Result<CustomModpack?> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Fetching custom modpack")
            
            val doc = firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .get()
                .await()
            
            val modpack = doc.toObject(CustomModpack::class.java)
                ?.copy(id = doc.id)
            
            Log.d(TAG, "Custom modpack fetched successfully")
            Result.success(modpack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun saveCustomModpack(modpackId: String, modpackData: Map<String, Any>): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Saving custom modpack")
            
            val dataWithTimestamp = modpackData.toMutableMap().apply {
                put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
            }
            
            firestore.collection("users")
                .document(userId)
                .collection("custom_modpacks")
                .document(modpackId)
                .set(dataWithTimestamp, SetOptions.merge())
                .await()
            
            Log.d(TAG, "Custom modpack saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

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
        
        activeListeners.add(listener)
        awaitClose { 
            listener.remove()
            activeListeners.remove(listener)
        }
    }

    suspend fun saveTemplate(template: ModpackTemplate): Result<String> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Saving template")
            
            val templateData = template.copy(userId = userId)
            val docRef = if (template.id.isEmpty()) {
                Log.d(TAG, "Creating new template")
                firestore.collection("users")
                    .document(userId)
                    .collection("templates")
                    .document()
            } else {
                Log.d(TAG, "Updating existing template")
                firestore.collection("users")
                    .document(userId)
                    .collection("templates")
                    .document(template.id)
            }
            
            docRef.set(templateData).await()
            
            Log.d(TAG, "Template saved successfully")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTemplate(templateId: String): Result<ModpackTemplate?> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Fetching template")
            
            val doc = firestore.collection("users")
                .document(userId)
                .collection("templates")
                .document(templateId)
                .get()
                .await()
            
            val template = doc.toObject(ModpackTemplate::class.java)?.copy(id = doc.id)
            
            Log.d(TAG, "Template fetched successfully")
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteTemplate(templateId: String): Result<Unit> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            Log.d(TAG, "Deleting template")
            
            firestore.collection("users")
                .document(userId)
                .collection("templates")
                .document(templateId)
                .delete()
                .await()
            
            Log.d(TAG, "Template deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
