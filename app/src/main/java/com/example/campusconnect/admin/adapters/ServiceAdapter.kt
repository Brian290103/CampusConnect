package com.example.campusconnect.admin.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.Constants
import com.example.campusconnect.databinding.BottomSheetServiceDetailsBinding
import com.example.campusconnect.databinding.RowServiceBinding
import com.example.campusconnect.models.Service
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class ServiceAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,  // Add FragmentManager as a parameter
    private var servicesList: ArrayList<Service>
) : RecyclerView.Adapter<ServiceAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceAdapter.MyViewHolder {
        val binding =
            RowServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: ServiceAdapter.MyViewHolder, position: Int) {
        val service = servicesList[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int {
        return servicesList.size
    }

    inner class MyViewHolder(var binding: RowServiceBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            binding.cardView.isClickable = true
            binding.cardView.setOnClickListener(this)
        }

        fun bind(service: Service) {
            binding.apply {
                txtName.text = service.name
            }
        }

        override fun onClick(p0: View?) {
            val service = servicesList[adapterPosition]

            val modalBottomSheet = ModalBottomSheet.newInstance(service.id)
            modalBottomSheet.show(fragmentManager, ModalBottomSheet.TAG)

        }

    }


    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: BottomSheetServiceDetailsBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = BottomSheetServiceDetailsBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val serviceId = arguments?.getString(ARG_SERVICE_ID)

            loadDetails(serviceId)

        }

        private fun loadDetails(serviceId: String?) {
            val query: Query =
                Constants().getDatabaseReference().child("services").orderByChild("id").equalTo(serviceId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(serviceDetails in snapshot.children){
                            val service = serviceDetails.getValue(Service::class.java)

                            binding.txtName.text =service?.name
                            binding.txtId.text =service?.id
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Failed to fetch details. Kindly try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        companion object {
            const val TAG = "ModalBottomSheet"
            private const val ARG_SERVICE_ID = "service_id"

            fun newInstance(serviceId: String): ModalBottomSheet {
                val fragment = ModalBottomSheet()
                val args = Bundle()
                args.putString(ARG_SERVICE_ID, serviceId)
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