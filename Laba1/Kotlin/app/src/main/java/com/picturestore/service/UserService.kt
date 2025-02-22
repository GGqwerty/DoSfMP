package com.picturestore.service

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.picturestore.data.Picture
import com.picturestore.data.User
import com.picturestore.data.User.Companion.fromFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserService {

    private val firestore = FirebaseFirestore.getInstance()

    fun getProfileStream(userId: String): Flow<User> = callbackFlow {
        val collectionReference = firestore.collection("users").document(userId)
        val listenerRegistration = collectionReference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val user = User.fromFirestore(data, snapshot.id)
                    trySend(user)
                }
            } else {
                trySend(User.empty(userId))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    suspend fun toggleFavorite(pictureId: String, userId: String) {
        val docRef = firestore.collection("users").document(userId)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) return

        val favourites = snapshot.get("favourites") as? List<*> ?: emptyList<Any>()
        val isFavorite = favourites.contains(pictureId)

        if (isFavorite) {
            docRef.update("favourites", FieldValue.arrayRemove(pictureId)).await()
        } else {
            docRef.update("favourites", FieldValue.arrayUnion(pictureId)).await()
        }
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .update(user.toFirestore())
            .await()
    }

    fun createInitialUser(user: User): Task<Void> {
        val docRef = firestore.collection("users").document(user.id)
        return docRef.get().continueWithTask { task ->
            if (!task.result.exists()) {
                val dictionary = user.toFirestore().toMutableMap()
                dictionary["emailVerified"] = false
                dictionary["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(dictionary)
            } else {
                Tasks.forResult(null)
            }
        }
    }

}