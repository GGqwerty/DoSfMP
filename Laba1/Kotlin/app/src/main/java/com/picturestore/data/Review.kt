package com.picturestore.data

data class Review(
    val id: String,
    val username: String,
    val text: String,
    val rating: Int
){
    fun toFirestore(): Map<String, Any>{
        return mapOf(
            "username" to username,
            "text" to text,
            "rating" to rating
        )
    }

    companion object {
        fun fromFirestore(data: Map<String, Any?>, id: String): Review{
            return Review(
                id=id,
                username = data["username"] as? String ?: "",
                text = data["text"] as? String ?: "",
                rating = (data["rating"] as? Number)?.toInt() ?: 0,
            )
        }
    }
}
