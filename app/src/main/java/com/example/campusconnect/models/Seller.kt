package com.example.campusconnect.models

import com.example.campusconnect.Constants

data class Seller(
    val id: String,
    val userId: String,
    val service: String,
    val description: String,
    val rating: Double,
    val coverImage: String,
    val date: String
) {
    constructor() : this("", "", "", "", 0.0, "", Constants().getDate()!!)
}