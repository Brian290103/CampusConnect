package com.example.campusconnect.models

import android.text.Editable

data class User(
    val id: String = "",
    val registrationNumber: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val phone: Int = 0,
    val address: String = "",
    val image: String = "",
    val seller: Boolean = false,
    val password: String = "",
    val role: String = "",
    val date: String = "",
) {
    // Secondary constructor for Firebase deserialization
    constructor() : this("", "", "", "", "", 0, "", "", false, "", "", "")
}
