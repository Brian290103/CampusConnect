package com.example.campusconnect.adapters

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.Constants
import com.example.campusconnect.databinding.BottomSheetSellersListBinding
import com.example.campusconnect.databinding.RowServiceCardBinding
import com.example.campusconnect.databinding.UserDetailsModalBottomSheetBinding
import com.example.campusconnect.models.Seller
import com.example.campusconnect.models.Service
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class ServiceAdapterCard(
    private val context: Context,
    private val fragmentManager: FragmentManager,  // Add FragmentManager as a parameter
    private var servicesList: ArrayList<Service>
) : RecyclerView.Adapter<ServiceAdapterCard.MyViewHolder>() {

    private lateinit var modalBottomSheet: ModalBottomSheet

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ServiceAdapterCard.MyViewHolder {
        val binding =
            RowServiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: ServiceAdapterCard.MyViewHolder, position: Int) {
        val service = servicesList[position]
        holder.bind(service)
    }

    override fun getItemCount(): Int {
        return servicesList.size
    }

    inner class MyViewHolder(var binding: RowServiceCardBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            binding.mainCardView.isClickable = true
            binding.mainCardView.setOnClickListener(this)
        }

        fun bind(service: Service) {
            binding.apply {
                txtName.text = service.name
                getSellersCount(service.name)
                val (color1, color2) = Constants().getRandomTwoHexColors()
                cardView.setCardBackgroundColor(Color.parseColor(color1))
                cardView1.setCardBackgroundColor(Color.parseColor(color1))
                imgIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(color2))

            }
        }

        private fun getSellersCount(name: String) {
            val query: Query =
                Constants().getDatabaseReference().child("sellers").orderByChild("service")
                    .equalTo(name)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = "${snapshot.childrenCount.toString()} sellers"
                    binding.txtSellersCount.text = count
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Failed to retrive details. Kindly try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })

        }

        override fun onClick(p0: View?) {
            val service = servicesList[adapterPosition]

            val existingFragment =
                fragmentManager.findFragmentByTag(ModalBottomSheet.TAG) as? ModalBottomSheet
            if (existingFragment != null) {
                existingFragment.updateContents(service.name)
            } else {
                ModalBottomSheet.newInstance(service.name)
                    .show(fragmentManager, ModalBottomSheet.TAG)
            }

        }

    }

    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: BottomSheetSellersListBinding? = null
        private val binding get() = _binding!!

        private lateinit var sellersAdapter: SellersAdapterCard
        private lateinit var sellersList: ArrayList<Seller>

        private var loggedInId: String? = ""
        private var isLoggedIn: Boolean = true
        private lateinit var prefs: SharedPreferences

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = BottomSheetSellersListBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Get user ID from arguments
            val serviceName = arguments?.getString(ARG_USER_ID)

            prefs = requireContext().getSharedPreferences("Campus Connect", Context.MODE_PRIVATE)
            loggedInId = prefs.getString("id", "")
            isLoggedIn = prefs.getBoolean("isLoggedIn", false)


            sellersList = ArrayList()
            sellersAdapter = SellersAdapterCard(
                requireContext(),
                requireActivity().supportFragmentManager,
                sellersList
            )

            binding.txtService.text = serviceName

            binding.recyclerView.layoutManager =
                GridLayoutManager(requireContext(), 2)
            binding.recyclerView.adapter = sellersAdapter

            loadDetails(serviceName)

        }

        fun updateContents(serviceName: String?) {
            loadDetails(serviceName)
        }


        private fun loadDetails(serviceName: String?) {
            val query: Query =
                Constants().getDatabaseReference().child("sellers").orderByChild("service")
                    .equalTo(serviceName)

            sellersList.clear()
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (sellersSnapshot in snapshot.children) {
                        val seller = sellersSnapshot.getValue(Seller::class.java)

                        if (isLoggedIn) {
                            if (!loggedInId.equals(seller!!.userId)
                            ) {
                                seller?.let { sellersList.add(it) }
                            }
                        } else {
                            sellersList.add(seller!!)
                        }

                    }

                    sellersAdapter.notifyDataSetChanged()

                    if (sellersList.isNotEmpty()) {
                        // Process the list of sellers
                        for (seller in sellersList) {
//                                Toast.makeText(context, "Name: ${seller.userId}", Toast.LENGTH_SHORT).show()
                            // Do other processing as needed
                        }
                    } else {
                        // Handle the case where no sellers were found for the given service name
                        Toast.makeText(
                            context,
                            "No sellers found for service: $serviceName",
                            Toast.LENGTH_SHORT
                        ).show()
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
            private const val ARG_USER_ID = "user_id"

            // Factory method to create a new instance of ModalBottomSheet with user ID
            fun newInstance(userId: String): ModalBottomSheet {
                val fragment = ModalBottomSheet()
                val args = Bundle()
                args.putString(ARG_USER_ID, userId)
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