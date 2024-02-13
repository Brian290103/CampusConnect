package com.example.campusconnect.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusconnect.R
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.example.campusconnect.databinding.FragmentInboxBinding
import com.example.campusconnect.databinding.FragmentOrdersBinding
import com.example.campusconnect.databinding.FragmentSearchBinding

class OrdersFragment : Fragment(R.layout.fragment_orders) {
    private lateinit var binding: FragmentOrdersBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOrdersBinding.bind(view)

        with(binding) {
            // Add your code here if needed
        }
    }

}