package com.example.campusconnect.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.campusconnect.Constants
import com.example.campusconnect.R
import com.example.campusconnect.activities.AccountActivity
import com.example.campusconnect.activities.BecomeASellerActivity
import com.example.campusconnect.activities.UnauthenticatedActivity
import com.example.campusconnect.admin.activities.AdminLoginActivity
import com.example.campusconnect.databinding.FragmentProfileBinding
import com.example.campusconnect.databinding.SettingsCardBinding
import com.example.campusconnect.models.User
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private var isLoggedIn: Boolean = true
    private var sellerMode: Boolean = true
    private var isSeller: Boolean = true
    private var role: String = ""
    private var id: String = ""
    private lateinit var prefs: SharedPreferences

    private var imgUrl: String = ""
    private var uri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)


        prefs = requireContext().getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        sellerMode = prefs.getBoolean("sellerMode", false)
        isSeller = prefs.getBoolean("isSeller", false)
        role = prefs.getString("role", "").toString()
        id = prefs.getString("id", "").toString()


        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    uri = data?.data
                    binding.imgProfile.setImageURI(uri)
                    uploadImage()
                } else {
                    Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
                }
            }

        binding.editImg.setOnClickListener {
            val photopicker = Intent(Intent.ACTION_PICK)
            photopicker.type = "image/*"
            activityResultLauncher.launch(photopicker)
        }



        with(binding) {
            join.title.text = "Join Campus Connect"
            join.icon.setImageResource(R.drawable.add_user)
            join.settingsCard.setOnClickListener {
                requireContext().startActivity(
                    Intent(
                        requireContext(),
                        UnauthenticatedActivity::class.java
                    )
                )
            }

            becomeASeller.title.text = "Become a Seller"
            becomeASeller.icon.setImageResource(R.drawable.generous)
            becomeASeller.settingsCard.setOnClickListener{
                requireContext().startActivity(Intent(requireContext(),BecomeASellerActivity::class.java))
            }

            interests.title.text = "My Interests"
            interests.icon.setImageResource(R.drawable.heart)

            account.title.text = "Account"
            account.icon.setImageResource(R.drawable.profile)

            account.settingsCard.setOnClickListener {
                if (isLoggedIn) {
                    requireContext().startActivity(
                        Intent(
                            requireContext(),
                            AccountActivity::class.java
                        )
                    )
                } else {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Kindly Login to proceed",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("Login") {
                        // Perform logout logic here
                        snackbar.dismiss() // Dismiss Snackbar after logout
                        requireContext().startActivity(
                            Intent(
                                requireContext(),
                                UnauthenticatedActivity::class.java
                            )
                        )
                    }
                    snackbar.show()
                }
            }

//            Toast.makeText(requireContext(), "role: ${role}", Toast.LENGTH_SHORT).show()

            if (isLoggedIn && role.equals("admin")) {
                admin.title.text = "Admin"
                admin.icon.setImageResource(R.drawable.protection)
                admin.settingsCard.setOnClickListener {
                    startActivity(Intent(requireContext(), AdminLoginActivity::class.java))
                }
            } else {
                admin.settingsCard.isVisible = false
            }



            support.title.text = "Support"
            support.icon.setImageResource(R.drawable.customer_service)


            if (isLoggedIn && isSeller) {
                sellerModeSwitch.isChecked = sellerMode
                sellerModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                    sellerMode = isChecked

                    // Save the updated sellerMode value to SharedPreferences
                    prefs.edit().putBoolean("sellerMode", sellerMode).apply()

                }
            } else {
                sellerModeSwitch.isVisible = false
            }


//            getDetails()
            //End of Binding
        }


    }

    private fun uploadImage() {
        if (uri != null) {
            val storageReference = FirebaseStorage.getInstance().reference
                .child("profile")
                .child(uri!!.lastPathSegment!!)

            binding.progressCircular.isVisible = true

            storageReference.putFile(uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isComplete);
                    val getImageUrl = uriTask.result

                    // Save the image URL or perform any additional logic if needed
                    Constants().getDatabaseReference().child("users").child(id).child("image")
                        .setValue(getImageUrl.toString())
                        .addOnSuccessListener {
                            binding.progressCircular.isVisible = false
                            Toast.makeText(
                                requireContext(),
                                "Image updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            binding.progressCircular.isVisible = false
                            Toast.makeText(
                                requireContext(),
                                "Failed to update image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    binding.progressCircular.isVisible = false
                    Toast.makeText(requireContext(), "Failed to upload", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Invalid image URI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDetails() {
        if (isLoggedIn) {

            binding.progressCircular.isVisible = true

            if (role.equals("admin")) {
                binding.txtCategory.text = "Admin"
            }

//            Toast.makeText(requireContext(), "id: ${id}", Toast.LENGTH_SHORT).show()
            Constants().getDatabaseReference().child("users").child(id)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.progressCircular.isVisible = false
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
//                            Log.d("userDetails","${user}")
                            val name = "${user!!.firstname} ${user!!.lastname}"
                            binding.txtName.text = name

                            if (!role.equals("admin")) {
                                if (user.seller) {
                                    binding.txtCategory.text = "Seller"
                                } else {
                                    binding.txtCategory.text = "Buyer"
                                }
                            }

                            if(!user.seller){
                                binding.becomeASeller.root.isVisible=true
                            }

                            if (!user?.image.isNullOrEmpty()) {
                                Picasso.get().load(user?.image).into(binding.imgProfile)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressCircular.isVisible = false
                        Toast.makeText(requireContext(), "Failed to fetch", Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        }
    }

    override fun onResume() {
        super.onResume()
getDetails()
        Toast.makeText(requireContext(), "On Resume", Toast.LENGTH_SHORT).show()
    }

}