package com.example.campusconnect.models

import com.example.campusconnect.Constants

data class Order(
    val id: String,
    val buyerId: String,
    val sellerId: String,
    val message: String,
    val date: String
) {
    constructor() : this("", "", "", "",  Constants().getDate()!!)
}