package com.picturestore.data

data class User(
    val id: String,
    val email: String,
    var phoneNumber: String,
    var firstName: String,
    var lastName: String,
    var birthDate: String,
    var country: String,
    var city: String,
    var address: String,
    var info: String,
    var gender: String,
    val favourites: List<String>
) {

    fun toFirestore(): Map<String, Any> {
        return mapOf(
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "birthDate" to birthDate,
            "phoneNumber" to phoneNumber,
            "address" to address,
            "country" to country,
            "city" to city,
            "info" to info,
            "gender" to gender,
            "favourites" to favourites
        )
    }

    companion object {
        fun empty(userId: String, email: String = ""): User {
            return User(
                id = userId,
                email = email,
                firstName = "",
                lastName = "",
                birthDate = "",
                phoneNumber = "",
                address = "",
                country = "",
                city = "",
                info = "",
                gender = "Unknown",
                favourites = emptyList()
            )
        }

        fun fromFirestore(data: Map<String, Any?>, id: String): User {
            return User(
                id = id,
                email = data["email"] as? String ?: "",
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                birthDate = data["birthDate"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                address = data["address"] as? String ?: "",
                country = data["country"] as? String ?: "",
                city = data["city"] as? String ?: "",
                info = data["info"] as? String ?: "",
                gender = data["gender"] as? String ?: "Unknown",
                favourites = (data["favourites"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
}
