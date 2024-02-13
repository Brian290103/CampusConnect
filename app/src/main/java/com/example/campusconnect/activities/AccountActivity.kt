package com.example.campusconnect.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.campusconnect.Constants
import com.example.campusconnect.databinding.ActivityAccountBinding
import com.example.campusconnect.models.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var prefs: SharedPreferences
    private var isLoggedIn: Boolean = true
    private var id: String = ""
    private var registrationNumber: String = ""
    private var image: String = ""
    private var role: String = ""
    private var date: String = ""
    private var seller: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar.toolbar)

        prefs = getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        id = prefs.getString("id", "").toString()



        with(binding) {
            myToolbar.toolbar.setNavigationOnClickListener { finish() }
            myToolbar.toolbar.title = "Account"


            btnLogout.setOnClickListener {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Are you sure you want to logout?",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setAction("OK") {
                    // Perform logout logic here
                    prefs.edit().putBoolean("isLoggedIn", false).apply()
                    snackbar.dismiss() // Dismiss Snackbar after logout
                    finish()
                    Toast.makeText(this@AccountActivity, "Log out successful", Toast.LENGTH_SHORT)
                        .show()
                }
                snackbar.show()
            }

            btnEdit.setOnClickListener {
                if (!binding.txtFirstname.isEnabled) {
                    txtFirstname.isEnabled = true
                    txtLastname.isEnabled = true
                    txtEmail.isEnabled = true
                    txtAddress.isEnabled = true
                    txtPhone.isEnabled = true
                    txtPassword.isEnabled = true
                    txtConfirmPassword.isEnabled = true
                    btnUpdate.isVisible = true
                } else {
                    txtFirstname.isEnabled = false
                    txtLastname.isEnabled = false
                    txtEmail.isEnabled = false
                    txtAddress.isEnabled = false
                    txtPhone.isEnabled = false
                    txtPassword.isEnabled = false
                    txtConfirmPassword.isEnabled = false
                    btnUpdate.isVisible = false
                }

                btnUpdate.setOnClickListener {
                    updateDetails()
                }
            }

        }

        getDetails()

    }

    private fun updateDetails() {
        if (isLoggedIn) {

            binding.progressCircular.isVisible = true

            if (binding.txtFirstname.text!!.isEmpty()) {
                Toast.makeText(this, "The firstname is missing", Toast.LENGTH_SHORT).show()
                binding.progressCircular.isVisible = false
            } else {
                val user: User = User(
                    id,
                    registrationNumber,
                    binding.txtFirstname.text.toString(),
                    binding.txtLastname.text.toString(),
                    binding.txtEmail.text.toString(),
                    binding.txtPhone.text.toString().toInt(),
                    binding.txtAddress.text.toString(),
                    image,
                    seller,
                    binding.txtPassword.text.toString(),
                    role,
                    date
                )
                Constants().getDatabaseReference().child("users").child(id).setValue(user)
                    .addOnSuccessListener {
                        binding.progressCircular.isVisible = false
                        Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        binding.progressCircular.isVisible = false
                        Toast.makeText(this, "Failed to update records", Toast.LENGTH_SHORT).show()
                    }
            }


        }
    }


    private fun getDetails() {
        if (isLoggedIn) {

            binding.progressCircular.isVisible = true

            Constants().getDatabaseReference().child("users").child(id)
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.progressCircular.isVisible = false
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
//                            Log.d("userDetails","${user}")
                            if (user != null) {
                                binding.txtFirstname.setText(user.firstname)
                                binding.txtLastname.setText(user.lastname)
                                binding.txtEmail.setText(user.email)
                                binding.txtAddress.setText(user.address)
                                binding.txtPhone.setText(user.phone.toString())
                                binding.txtPassword.setText(user.password)
                                binding.txtConfirmPassword.setText(user.password)
                                seller = user.seller
                                registrationNumber = user.registrationNumber
                                image = user.image
                                role = user.role
                                date = user.date
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressCircular.isVisible = false
                        Toast.makeText(this@AccountActivity, "Failed to fetch", Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        }
    }

}