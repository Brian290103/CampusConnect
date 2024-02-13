package com.example.campusconnect.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.campusconnect.Constants
import com.example.campusconnect.R
import com.example.campusconnect.admin.adapters.UserAdapter
import com.example.campusconnect.databinding.ActivityBecomeAsellerBinding
import com.example.campusconnect.databinding.ActivityManageUsersBinding
import com.example.campusconnect.models.Seller
import com.example.campusconnect.models.Service
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class BecomeASellerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBecomeAsellerBinding

    private var imgUrl: String = ""
    private var uri: Uri? = null

    private lateinit var prefs: SharedPreferences

    private var id: String = ""
    private var selectedService: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBecomeAsellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar.toolbar)

        prefs = getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
        id = prefs.getString("id", "").toString()


//        val adapter = ArrayAdapter(this, R.layout.list_item, items)
//        (binding.dropdownServices as? AutoCompleteTextView)?.setAdapter(adapter)
        getServices()



        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    uri = data?.data
                    binding.coverImage.setImageURI(uri)

                } else {
                    Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
                }
            }

        binding.coverImage.setOnClickListener {
            val photopicker = Intent(Intent.ACTION_PICK)
            photopicker.type = "image/*"
            activityResultLauncher.launch(photopicker)
        }


        with(binding) {
            myToolbar.toolbar.setNavigationOnClickListener { finish() }
            myToolbar.toolbar.title = "Become a Seller"

            btnContinue.setOnClickListener {

                if (selectedService.equals("") || txtDescription.text.toString()
                        .equals("") || uri == null
                ) {
                    Toast.makeText(
                        this@BecomeASellerActivity,
                        "Kindly fill in all the required fields",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uploadImage()
                }
            }
        }
    }

    private fun getServices() {
        binding.progressCircular.isVisible = true
        val servicesList = ArrayList<Service>() // Initialize servicesList

        Constants().getDatabaseReference().child("services").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false

                servicesList.clear()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)

                    service?.let {
                        servicesList.add(it)
                    }
                }

                val serviceNames = servicesList.map { it.name } // Assuming Service class has a 'name' property
                val serviceNamesList = serviceNames.toList()

                val adapter = ArrayAdapter(this@BecomeASellerActivity, R.layout.list_item, serviceNamesList)
                (binding.dropdownServices as? AutoCompleteTextView)?.setAdapter(adapter)
//                (binding.dropdownServices as? MaterialAutoCompleteTextView)?.setSimpleItems(serviceNamesList)

                if (serviceNames.isNotEmpty()) {
                    selectedService = serviceNames[0]

                    (binding.dropdownServices as? AutoCompleteTextView)?.setText(serviceNames[0], false)

                    (binding.dropdownServices as? AutoCompleteTextView)?.setOnItemClickListener { _, _, position, _ ->
                        selectedService = serviceNames[position]
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BecomeASellerActivity, "Failed to Fetch", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadImage() {
        binding.progressCircular.isVisible=true
        val query: Query =
            Constants().getDatabaseReference().child("sellers").orderByChild("userId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(
                        this@BecomeASellerActivity,
                        "You are already a seller",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressCircular.isVisible=false
                } else {
                    if (uri != null) {
                        val storageReference = FirebaseStorage.getInstance().reference
                            .child("profile")
                            .child(uri!!.lastPathSegment!!)

                        storageReference.putFile(uri!!)
                            .addOnSuccessListener { taskSnapshot ->
                                val uriTask = taskSnapshot.storage.downloadUrl
                                while (!uriTask.isComplete);
                                val getImageUrl = uriTask.result

                                // Save the image URL or perform any additional logic if needed

                                val uniqueId = Constants().getDatabaseReference().push().key

                                val seller = Seller(
                                    uniqueId!!,
                                    id,
                                    selectedService,
                                    binding.txtDescription.text.toString(),
                                    0.0,
                                    getImageUrl.toString(),
                                    Constants().getDate().toString()
                                )


                                Constants().getDatabaseReference().child("sellers").child(uniqueId)
                                    .setValue(seller)
                                    .addOnSuccessListener {
                                        binding.progressCircular.isVisible=false
                                        Constants().getDatabaseReference().child("users").child(id).child("seller").setValue(true)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@BecomeASellerActivity,
                                                    "Your application is successful",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                finish()
                                            }
                                            .addOnFailureListener{
                                                Toast.makeText(
                                                    this@BecomeASellerActivity,
                                                    "Failed. Kindly try again later",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }


                                    }
                                    .addOnFailureListener {
                                        binding.progressCircular.isVisible=false
                                        Toast.makeText(
                                            this@BecomeASellerActivity,
                                            "Failed. Kindly try again later",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                binding.progressCircular.isVisible = false
                                Toast.makeText(this@BecomeASellerActivity, "Failed to upload", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        binding.progressCircular.isVisible=false
                        Toast.makeText(
                            this@BecomeASellerActivity,
                            "Invalid image URI",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressCircular.isVisible=false
                Toast.makeText(
                    this@BecomeASellerActivity,
                    "Failed. Kindly try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })

    }

}