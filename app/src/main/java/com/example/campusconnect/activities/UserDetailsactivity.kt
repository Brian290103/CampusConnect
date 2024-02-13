package com.example.campusconnect.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.campusconnect.Constants
import android.Manifest
import com.example.campusconnect.admin.adapters.UserAdapter
import com.example.campusconnect.databinding.ActivityUserDetailsactivityBinding
import com.example.campusconnect.databinding.BottomSheetHireMeBinding
import com.example.campusconnect.models.Order
import com.example.campusconnect.models.Seller
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserDetailsactivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsactivityBinding

    var sellerId: String = ""
   public var sellerPhoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityUserDetailsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val id = intent.getStringExtra("id")
        sellerId = intent.getStringExtra("userId")!!

        loadDetails(id)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            btnHireMe.setOnClickListener {
                val modalBottomSheet = ModalBottomSheet.newInstance(sellerPhoneNumber)
                modalBottomSheet.show(supportFragmentManager, UserAdapter.ModalBottomSheet.TAG)

            }
        }
    }

    private fun loadDetails(id: String?) {
        val query: Query =
            Constants().getDatabaseReference().child("sellers").orderByChild("id").equalTo(id)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (sellerDetail in snapshot.children) {
                        val seller = sellerDetail.getValue(Seller::class.java)


                        if (seller!!.coverImage.isNotEmpty()) {
                            Picasso.get().load(seller!!.coverImage).into(binding.imgCoverPhoto)
                        }

                        binding.txtDescription.text = seller!!.description

                        sellerId=seller.userId

                        getUserDetails(seller.userId!!)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserDetailsactivity,
                    "Failed to fetch details. Kindly try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getUserDetails(id: String) {
        val query: Query =
            Constants().getDatabaseReference().child("users").orderByChild("id").equalTo(id)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false
                if (snapshot.exists()) {
                    for (userDetails in snapshot.children) {
                        val user = userDetails.getValue(User::class.java)

                        sellerPhoneNumber=user!!.phone.toString()

                        Toast.makeText(this@UserDetailsactivity, "phone: ${sellerPhoneNumber}", Toast.LENGTH_SHORT).show()

                        val name = "${user!!.firstname} ${user!!.lastname}"
                        binding.toolbar.title = name
                        binding.collapsingToolbar.title = name
                        if (user!!.image.isNotEmpty()) {
                            Picasso.get().load(user!!.image).into(binding.imgProfile)
                        }

                        binding.txtName.text = name
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserDetailsactivity,
                    "Failed to fetch details. Kindly try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: BottomSheetHireMeBinding? = null
        private val binding get() = _binding!!
        private var isLoggedIn: Boolean = true
        private var loggedInUserId: String? = ""
        private lateinit var prefs: SharedPreferences
        private  val SEND_SMS_PERMISSION_REQUEST_CODE=1

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = BottomSheetHireMeBinding.inflate(inflater, container, false)
            return binding.root
        }

        // Function to check if a permission is granted
        fun checkPermission(permission: String): Boolean {
            val check = ContextCompat.checkSelfPermission(requireContext(), permission)
            return check == PackageManager.PERMISSION_GRANTED
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Get user ID from arguments
            val sellerPhoneNumber = arguments?.getString(ARG_USER_ID)

            Toast.makeText(requireContext(), "sellerPhoneNumber: ${sellerPhoneNumber}", Toast.LENGTH_SHORT).show()

            prefs = requireContext().getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
            isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            loggedInUserId = prefs.getString("id", "false")


            val txtMessage = binding.txtMessage
            val btnPlaceOrder = binding.btnPlaceOrder

            btnPlaceOrder.setOnClickListener {

                Toast.makeText(requireContext(), "phone num ${sellerPhoneNumber}" , Toast.LENGTH_SHORT).show()

                if(!isLoggedIn){
                    val snackbar = Snackbar.make(
                        binding.root,
                        "You are not logged in",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("Login") {
                        requireContext().startActivity(Intent(requireContext(),UnauthenticatedActivity::class.java))
                    }
                    snackbar.show()
                    return@setOnClickListener
                }

                val message = txtMessage.text.toString()

                binding.progressCircular.isVisible = true

                if (message.isEmpty()) {
                    binding.progressCircular.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        "One of the fields is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val uniqueId=Constants().getDatabaseReference().push().key
                val order=Order(uniqueId!!,loggedInUserId!!, requireActivity().intent.getStringExtra("userId")!!,message,Constants().getDate()!!)

                if (checkPermission(Manifest.permission.SEND_SMS)) {
                    // Permission is granted, send SMS
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage("0${sellerPhoneNumber}", null, "${uniqueId} - Campus Connect. Congratulations! A Job order has been placed at ${Constants().getDate()}. Kindly check you inbox. Message Description: ${message}", null, null)
//                    Toast.makeText(requireContext(), "Message sent successfully", Toast.LENGTH_SHORT).show()

                    Constants().getDatabaseReference().child("orders").child(uniqueId).setValue(order)
                        .addOnSuccessListener {
                            binding.progressCircular.isVisible = false
                            Toast.makeText(requireContext(), "You order has been placed", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        .addOnFailureListener{
                            binding.progressCircular.isVisible = false
                            Toast.makeText(requireContext(), "Failed to place order. Kindly try again later", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.SEND_SMS),
                        SEND_SMS_PERMISSION_REQUEST_CODE
                    )
                }


            }
        }

        companion object {
            const val TAG = "ModalBottomSheet"
            private const val ARG_USER_ID = "user_id"

            // Factory method to create a new instance of ModalBottomSheet with user ID
            fun newInstance(sellerPhoneNumber:String): ModalBottomSheet {
                val fragment = ModalBottomSheet()
                val args = Bundle()
                args.putString(ARG_USER_ID, sellerPhoneNumber)
                fragment.arguments = args
                return fragment
            }

        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }


}