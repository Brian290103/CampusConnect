package com.example.campusconnect.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.Constants
import com.example.campusconnect.R
import com.example.campusconnect.adapters.SellersAdapterCard
import com.example.campusconnect.adapters.ServiceAdapterCard
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.example.campusconnect.databinding.UserDetailsModalBottomSheetBinding
import com.example.campusconnect.models.Seller
import com.example.campusconnect.models.Service
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var serviceAdapter: ServiceAdapterCard
    private lateinit var servicesList: ArrayList<Service>

    private lateinit var sellersAdapter: SellersAdapterCard
    private lateinit var sellersList: ArrayList<Seller>

    private var loggedInId: String? = ""
    private var isLoggedIn: Boolean = true
    private lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        prefs = requireContext().getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
        loggedInId = prefs.getString("id", "")
        isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        servicesList = ArrayList()
        serviceAdapter = ServiceAdapterCard(
            requireContext(),
            requireActivity().supportFragmentManager,
            servicesList
        )

        sellersList = ArrayList()
        sellersAdapter = SellersAdapterCard(
            requireContext(),
            requireActivity().supportFragmentManager,
            sellersList
        )


        with(binding) {
            rvServices.layoutManager = GridLayoutManager(requireContext(), 2)
            rvServices.adapter = serviceAdapter

            rvSellers.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvSellers.adapter = sellersAdapter

        }
        getServices()
        getSellers()
    }

    private fun getServices() {

        Constants().getDatabaseReference().child("services").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false

                servicesList.clear()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)

                    servicesList.add(service!!)
                }

                serviceAdapter.notifyDataSetChanged()
//                    for (service in servicesList) {
//                        Log.d(
//                            "ProjectDetails",
//                            "ID: ${service.id}, Name: ${service.name}, Email: ${service.date}"
//                        )
//                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to Fetch", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun getSellers() {

        Constants().getDatabaseReference().child("sellers").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false

                sellersList.clear()
                for (sellersSnapshot in snapshot.children) {
                    val seller = sellersSnapshot.getValue(Seller::class.java)

                    if(isLoggedIn){
                        if (!loggedInId.equals(seller!!.userId)
                        ) {
                            seller?.let { sellersList.add(it) }
                        }
                    }else{
                        sellersList.add(seller!!)
                    }



                }

                sellersAdapter.notifyDataSetChanged()
                for (service in sellersList) {
                    Log.d(
                        "ProjectDetails",
                        "ID: ${service.id}, Name: ${service.userId}, Email: ${service.date}"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to Fetch", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }



}