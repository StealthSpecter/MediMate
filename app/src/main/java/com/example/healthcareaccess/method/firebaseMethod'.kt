package com.example.healthcareaccess.method

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseMethod {
    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().uid
    }

    fun currentUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId()!!)
    }
}
