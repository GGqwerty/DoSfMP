package com.picturestore.data

data class Picture(
    val id: String,
    val name: String,
    val authors: List<String>,
    val materials: List<String>,
    val genre: String,
    val style: String,
    val type: String,
    val size: String,
    val year: Int,
    val specialInformation: String,
    val moreInformation: String,
    val urlsImage: List<String>,
    var reviews: List<Review>
){
    fun toFirestore(): Map<String, Any>{
        return mapOf(
            "name" to name,
            "authors" to authors,
            "materials" to materials,
            "genre" to genre,
            "style" to style,
            "type" to type,
            "size" to size,
            "year" to year,
            "specialInformation" to specialInformation,
            "moreInformation" to moreInformation,
            "urlsImage" to urlsImage,
            "reviews" to reviews
            )
    }

    companion object {

        fun empty(id: String): Picture{
            return Picture(
                id = id,
                name = "",
                authors = emptyList(),
                materials = emptyList(),
                genre = "",
                style = "",
                type = "",
                size = "",
                year = 0,
                specialInformation = "",
                moreInformation = "",
                urlsImage = emptyList(),
                reviews = emptyList()
            )
        }

        fun fromFirestore(data: Map<String, Any?>, id: String): Picture{
            return Picture(
                id = id,
                name = data["name"] as? String ?: "",
                authors = (data["authors"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                materials = (data["materials"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                genre = data["genre"] as? String ?: "",
                style = data["style"] as? String ?: "",
                type = data["type"] as? String ?: "",
                size = data["size"] as? String ?: "",
                year = (data["year"] as? Number)?.toInt() ?: 0,
                specialInformation = data["specialInformation"] as? String ?: "",
                moreInformation = data["moreInformation"] as?  String ?: "",
                urlsImage = (data["urlsImage"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                reviews = (data["reviews"] as? List<*>)?.mapNotNull { it as? Review } ?: emptyList()
                )
        }
    }
}
