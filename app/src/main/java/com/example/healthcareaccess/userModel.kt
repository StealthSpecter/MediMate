package com.example.healthcareaccess

import com.google.firebase.Timestamp

class UserModel(
    val phone: String,
    var username: String,
    val createdTimestamp: Timestamp?,
    s: String
) {
    // Empty constructor not needed in Kotlin
}