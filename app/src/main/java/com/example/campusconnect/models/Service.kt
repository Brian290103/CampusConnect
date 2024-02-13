package com.example.campusconnect.models

import android.text.Editable
import com.example.campusconnect.Constants

data class Service(
    val id: String = "",
    val name: String = "",
    val date: String = "",
) {
    // Secondary constructor for Firebase deserialization
    constructor() : this("", "", Constants().getDate()!!)
}
