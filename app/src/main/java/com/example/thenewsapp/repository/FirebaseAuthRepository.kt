package com.example.thenewsapp.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

data class RemoteUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val bio: String?,
    val photoUrl: String?
)

class FirebaseAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    suspend fun register(email: String, password: String, displayName: String?): Result<RemoteUser> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, password).await()
            val user = res.user ?: throw IllegalStateException("No user")
            val profile = mapOf(
                "email" to user.email,
                "displayName" to displayName,
                "bio" to null,
                "photoUrl" to null
            )
            userDoc(user.uid).set(profile).await()
            Result.success(RemoteUser(user.uid, user.email, displayName, null, null))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<RemoteUser> {
        return try {
            val res = auth.signInWithEmailAndPassword(email, password).await()
            val user = res.user ?: throw IllegalStateException("No user")
            val snap = userDoc(user.uid).get().await()
            val displayName = snap.getString("displayName")
            val bio = snap.getString("bio")
            val photoUrl = snap.getString("photoUrl")
            Result.success(RemoteUser(user.uid, user.email, displayName, bio, photoUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun currentUser(): RemoteUser? {
        val user = auth.currentUser ?: return null
        val snap = userDoc(user.uid).get().await()
        return RemoteUser(user.uid, user.email, snap.getString("displayName"), snap.getString("bio"), snap.getString("photoUrl"))
    }

    suspend fun updateProfile(displayName: String?, bio: String?, photoUrl: String?): Result<RemoteUser> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not logged in"))
        return try {
            val data = hashMapOf<String, Any?>(
                "displayName" to displayName,
                "bio" to bio,
                "photoUrl" to photoUrl
            )
            userDoc(user.uid).set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(RemoteUser(user.uid, user.email, displayName, bio, photoUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(imageUri: Uri): Result<String> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not logged in"))
        return try {
            val ref = storage.reference.child("profilePhotos/${'$'}{user.uid}.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}