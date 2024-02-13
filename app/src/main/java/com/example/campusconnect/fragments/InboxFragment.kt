package com.example.campusconnect.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.campusconnect.R
import com.example.campusconnect.databinding.FragmentHomeBinding
import com.example.campusconnect.databinding.FragmentInboxBinding

class InboxFragment : Fragment(R.layout.fragment_inbox) {
    private lateinit var binding: FragmentInboxBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInboxBinding.bind(view)

        with(binding) {
            // Add your code here if needed
        }
    }

}