package com.example.campusconnect.admin.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.Constants
import com.example.campusconnect.databinding.RowUserBinding
import com.example.campusconnect.databinding.UserDetailsModalBottomSheetBinding
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener


class UserAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,  // Add FragmentManager as a parameter
    private var originalUsersList: ArrayList<User>
) : RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.MyViewHolder {
        val binding =
            RowUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: UserAdapter.MyViewHolder, position: Int) {
        val user = originalUsersList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return originalUsersList.size
    }

    inner class MyViewHolder(var binding: RowUserBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            binding.cardView.isClickable = true
            binding.cardView.setOnClickListener(this)
        }

        private var selectedRating = 1

        fun bind(user: User) {
            binding.apply {
                val username = user.firstname + user.lastname
                txtFullname.text = username
                txtRegistrationNumber.text = user.registrationNumber
            }
        }

        override fun onClick(p0: View?) {
            val user = originalUsersList[adapterPosition]
//            val intent = Intent(context, AdminDashboardActivity::class.java)
//            intent.putExtra("name", user.firstname)
//            context.startActivity(intent)

            val modalBottomSheet = ModalBottomSheet.newInstance(user.id)
            modalBottomSheet.show(fragmentManager, ModalBottomSheet.TAG)

        }

    }

    fun filterList(filterList: ArrayList<User>) {
        originalUsersList = filterList
        notifyDataSetChanged()
    }

    class ModalBottomSheet : BottomSheetDialogFragment() {

        private var _binding: UserDetailsModalBottomSheetBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = UserDetailsModalBottomSheetBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Get user ID from arguments
            val userId = arguments?.getString(ARG_USER_ID)

            loadDetails(userId)

        }

        private fun loadDetails(userId: String?) {
            val query: Query =
                Constants().getDatabaseReference().child("users").orderByChild("id").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(userDetail in snapshot.children){
                            val user = userDetail.getValue(User::class.java)

                            val name= "${user?.firstname} ${user?.lastname}"
                            binding.txtName.text =name
                            binding.txtRegistrationNumber.text =user?.registrationNumber

                            if(user?.seller == true){
                                binding.seller.isVisible= true
                            }
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