package com.example.campusconnect.admin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.Constants
import com.example.campusconnect.activities.UnauthenticatedActivity
import com.example.campusconnect.admin.adapters.ServiceAdapter
import com.example.campusconnect.databinding.ActivityManageServicesBinding
import com.example.campusconnect.databinding.BottomSheetAddServiceBinding
import com.example.campusconnect.models.Service
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ManageServicesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageServicesBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var servicesList: ArrayList<Service>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)

        servicesList = ArrayList()
        serviceAdapter = ServiceAdapter(this, supportFragmentManager, servicesList)

        with(binding) {
            toolbar.toolbar.setNavigationOnClickListener { finish() }
            toolbar.toolbar.title = "Manage Services"

            recyclerView.layoutManager = LinearLayoutManager(this@ManageServicesActivity)
            binding.recyclerView.adapter = serviceAdapter

            btnAddService.setOnClickListener{
                val modalBottomSheet = ModalBottomSheet.newInstance()
                modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
            }
        }
        getServices()
    }

    private fun getServices() {

        Constants().getDatabaseReference().child("services").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false

                servicesList.clear()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)

                    servicesList.add(service!!)
                }

                serviceAdapter.notifyDataSetChanged()
                for (service in servicesList) {
                    Log.d(
                        "ProjectDetails",
                        "ID: ${service.id}, Name: ${service.name}, Email: ${service.date}"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageServicesActivity, "Failed to Fetch", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: BottomSheetAddServiceBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = BottomSheetAddServiceBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.btnAddService.setOnClickListener {
                val serviceName = binding.txtServiceName.text.toString().trim()

                binding.progressCircular.isVisible=true

                if (serviceName.isEmpty()) {
                    binding.progressCircular.isVisible=false
                    Toast.makeText(
                        requireContext(),
                        "One of the fields is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val query: Query =
                    Constants().getDatabaseReference().child("services").orderByChild("name")
                        .equalTo(serviceName);
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()) {
                            Toast.makeText(requireContext(), "That service name already exists", Toast.LENGTH_SHORT).show()
                            binding.progressCircular.isVisible=false
                        } else {
                            val uniqueId=Constants().getDatabaseReference().push().key!!
                            val service=Service(uniqueId,serviceName,Constants().getDate().toString())
                           Constants().getDatabaseReference().child("services").child(uniqueId).setValue(service)
                               .addOnSuccessListener {
                                   binding.progressCircular.isVisible=false
                                   Toast.makeText(requireContext(), "Service add successfully", Toast.LENGTH_SHORT).show()
                                   dismiss()
                               }
                               .addOnFailureListener{
                                   binding.progressCircular.isVisible=false
                                   Toast.makeText(requireContext(), "Failed to add service", Toast.LENGTH_SHORT).show()
                               }
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