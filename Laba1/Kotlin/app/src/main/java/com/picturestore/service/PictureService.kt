package com.picturestore.service

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.picturestore.data.Picture
import com.picturestore.data.Review
import com.picturestore.data.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PictureService {
    private val firestore = FirebaseFirestore.getInstance()

    fun addReview(pictureId: String, user: String, rating: Int, comment: String, onComplete: (Boolean) -> Unit) {
        val review = Review("", user, comment, rating)

        firestore.collection("pictures")
            .document(pictureId)
            .collection("reviews")
            .add(review.toFirestore())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getPicture(pictureId: String): Flow<Picture> = callbackFlow {
        val collectionReference = firestore.collection("pictures").document(pictureId)
        val listenerRegistration = collectionReference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val picture = Picture.fromFirestore(data, snapshot.id)

                    val reviewsRef = firestore.collection("pictures")
                        .document(pictureId).collection("reviews")

                    reviewsRef.get().addOnSuccessListener { reviewDocs ->
                        val reviews = reviewDocs.map { doc ->
                            Review.fromFirestore(doc.data, doc.id)
                        }

                        val pictureWithReviews = picture.copy(reviews = reviews)
                        trySend(pictureWithReviews)
                    }
                }
            } else {
                trySend(Picture.empty(pictureId))
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    fun getPictures(): Flow<List<Picture>> = callbackFlow {
        val collectionReference = firestore.collection("pictures")
        val listenerRegistration = collectionReference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val pictures = snapshot?.documents?.map { doc ->
                Picture.fromFirestore(doc.data ?: emptyMap(), doc.id)
            } ?: emptyList()

            val picturesBuf = mutableListOf<Picture>()
            val tasks = pictures.map { pictureDoc ->
                val pictureId = pictureDoc.id

                firestore.collection("pictures").document(pictureId).collection("reviews").get()
                    .continueWith { task ->
                        val reviews = if (task.isSuccessful) {
                            task.result?.map { doc -> Review.fromFirestore(doc.data, doc.id) } ?: emptyList()
                        } else {
                            emptyList()
                        }
                        pictureDoc.copy(reviews = reviews)
                    }
            }

            Tasks.whenAllSuccess<Picture>(tasks).addOnSuccessListener { results ->
                picturesBuf.addAll(results)
                trySend(picturesBuf)
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    fun getFavoritePictures(userId: String): Flow<List<Picture>> = callbackFlow {

        var pictureListener: ListenerRegistration? = null
        val userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { userSnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (userSnapshot == null || !userSnapshot.exists()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val pictureIds = userSnapshot.get("favourites") as? List<String> ?: emptyList()

                if (pictureIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                pictureListener = firestore.collection("pictures")
                    .whereIn(FieldPath.documentId(), pictureIds)
                    .addSnapshotListener { pictureSnapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val pictures = pictureSnapshot?.documents?.map { doc ->
                            Picture.fromFirestore(
                                doc.data ?: emptyMap(), doc.id
                            )
                        } ?: emptyList()
                        trySend(pictures)
                    }

            }

        awaitClose {
            userListener.remove()
            pictureListener?.remove()
        }
    }



}