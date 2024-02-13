package com.example.campusconnect

import android.os.Build
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Constants {
    fun getDate(): String? {
        var date: String? = null

        val dtf: DateTimeFormatter?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        } else {
            dtf = null
        }

        val now: LocalDateTime?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDateTime.now()
        } else {
            now = null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = dtf?.format(now)
        }

        return date
    }


    fun  getDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    fun getRandomHexColor(): String {
//        val hexColors = listOf("#FF5733", "#33FF57", "#5733FF", "#33FFFF", "#FFFF33", "#FF33FF", "#999999")
        val hexColors = listOf("#FFEBEE", "#FCE4EC", "#FAFAFA", "#F3E5F5", "#E3F2FD", "#E0F7FA", "#E0F2F1","#F9FBE7","#FFFDE7","#FFF8E1","#FFF3E0","#FBE9E7","#EFEBE9","#E8F5E9")

        // Choose a random index from the list
//        val randomIndex = (0 until hexColors.size).random()
        val randomIndex = (hexColors.indices).random()

        // Return the hex color at the random index
        return hexColors[randomIndex]
    }

    fun getRandomTwoHexColors(): Pair<String, String> {
        // List of hex colors for the first color
        val hexColorsList1 = listOf("#FFEBEE", "#FCE4EC", "#FAFAFA", "#F3E5F5", "#E3F2FD", "#E0F7FA", "#E0F2F1","#F9FBE7","#FFFDE7","#FFF8E1","#FFF3E0","#FBE9E7","#EFEBE9","#E8F5E9")

        // List of hex colors for the second color
        val hexColorsList2 = listOf("#FF1744", "#EC407A", "#212121", "#D500F9", "#2979FF", "#00E5FF", "#1DE9B6","#C6FF00","#FFEA00","#FFC400","#FF9100","#FF3D00","#3E2723","#00E676")

//        // Choose random indexes from the lists
//        val randomIndex1 = (hexColorsList1.indices).random()
//        val randomIndex2 = (hexColorsList2.indices).random()

        // Choose a random index that is valid for both lists
        val randomIndex = (hexColorsList1.indices).random()

// Retrieve colors at the random index from both lists
        val color1 = hexColorsList1[randomIndex]
        val color2 = hexColorsList2[randomIndex]

        // Return the pair of hex colors
        return Pair(hexColorsList1[randomIndex], hexColorsList2[randomIndex])
    }

}