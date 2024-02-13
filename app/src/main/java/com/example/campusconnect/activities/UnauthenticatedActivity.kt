package com.example.campusconnect.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.campusconnect.Constants
import com.example.campusconnect.R
import com.example.campusconnect.admin.adapters.UserAdapter
import com.example.campusconnect.databinding.ActivityMainBinding
import com.example.campusconnect.databinding.ActivityUnauthenticatedBinding
import com.example.campusconnect.databinding.DialogSignInBinding
import com.example.campusconnect.databinding.UserDetailsModalBottomSheetBinding
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class UnauthenticatedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnauthenticatedBinding
    private var isLoggedIn: Boolean = true
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUnauthenticatedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)

         prefs = getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
        isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        with(binding) {
            binding.toolbar.toolbar.setNavigationOnClickListener {
                finish()
            }

            signUp.setOnClickListener {
                startActivity(Intent(this@UnauthenticatedActivity, RegisterActivity::class.java))
            }


            signIn.setOnClickListener {
                if(isLoggedIn){
                    val snackbar = Snackbar.make(
                        binding.root,
                        "You are already logged in",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setAction("Logout") {
                        // Perform logout logic here
                        prefs.edit().putBoolean("isLoggedIn", false).apply()
                        snackbar.dismiss() // Dismiss Snackbar after logout
                        finish()
                        Toast.makeText(this@UnauthenticatedActivity, "Log out successful", Toast.LENGTH_SHORT).show()
                    }
                    snackbar.show()
                }else{
                    val modalBottomSheet = ModalBottomSheet.newInstance()
                    modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
                }
              
            }

            // end of binding
        }
    }


    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: DialogSignInBinding? = null
        private val binding get() = _binding!!
        private lateinit var prefs: SharedPreferences

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = DialogSignInBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            prefs = requireContext().getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)

            val registrationNumberEditText = binding.txtRegistrationNumber
            val passwordEditText = binding.txtPassword
            val loginButton = binding.btnLogin

            loginButton.setOnClickListener {
                val registrationNumber = registrationNumberEditText.text.toString()
                val password = passwordEditText.text.toString()

                binding.progressCircular.isVisible=true

                if (registrationNumber.isEmpty() || password.isEmpty()) {
                    binding.progressCircular.isVisible=false
                    Toast.makeText(
                        requireContext(),
                        "One of the fields is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val query: Query =
                    Constants().getDatabaseReference().child("users").orderByChild("registrationNumber")
                        .equalTo(registrationNumber);
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.progressCircular.isVisible=false

                        if (snapshot.exists()) {

                            for(userDetails in snapshot.children){
                                val user = userDetails.getValue(User::class.java)

                                if (user != null) {
                                    if(user.password.equals(password)){
                                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                                        prefs.edit().putBoolean("isLoggedIn", true).apply()
                                        prefs.edit().putString("role", user.role).apply()
                                        prefs.edit().putString("id", user.id).apply()
                                        prefs.edit().putBoolean("isSeller", user.seller).apply()
                                        dismiss()
                                        requireActivity().finish()
                                    }else{
                                        Toast.makeText(requireContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Kindly Register first to continue",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressCircular.isVisible=false
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                    }})
            }
        }

        companion object {
            const val TAG = "ModalBottomSheet"
            private const val ARG_USER_ID = "user_id"

            // Factory method to create a new instance of ModalBottomSheet with user ID
            fun newInstance(): ModalBottomSheet {
                val fragment = ModalBottomSheet()
                val args = Bundle()
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