package com.example.campusconnect.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.campusconnect.Constants
import com.example.campusconnect.R
import com.example.campusconnect.databinding.ActivityRegisterBinding
import com.example.campusconnect.databinding.ActivityUnauthenticatedBinding
import com.example.campusconnect.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)

        with(binding) {
            binding.toolbar.toolbar.setNavigationOnClickListener {
                finish()
            }

            signUp.setOnClickListener {
                signUp()
            }
            // end of binding
        }

    }

    private fun signUp() {
        with(binding) {
            val id = databaseReference.push().key
            val firstName = txtFirstname.text.toString()
            val registrationNumber = txtRegistrationNumber.text.toString()
            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPassword.text.toString()

            if (firstName.isEmpty() || registrationNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    this@RegisterActivity,
                    "One of the fields is empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(
                    this@RegisterActivity,
                    "The passwords don't match",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val user = User(
                    id!!,
                    registrationNumber,
                    firstName,
                    "",
                    "",
                    0,
                    "",
                    "",
                    false,
                    password,
                    "user",
                    Constants().getDate()!!,
                )

                val query: Query = databaseReference.orderByChild("registrationNumber")
                    .equalTo(registrationNumber);
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Your are already registered. Kindly login to continue",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            databaseReference.child(id).setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registration Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@RegisterActivity, "Failed", Toast.LENGTH_SHORT).show()
                    }

                })


            }
        }

    }
}