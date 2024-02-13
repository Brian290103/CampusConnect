package com.example.campusconnect.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusconnect.Constants
import com.example.campusconnect.activities.UserDetailsactivity
import com.example.campusconnect.databinding.RowSellerBinding
import com.example.campusconnect.databinding.RowServiceCardBinding
import com.example.campusconnect.models.Seller
import com.example.campusconnect.models.Service
import com.example.campusconnect.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class SellersAdapterCard(
    private val context: Context,
    private val fragmentManager: FragmentManager,  // Add FragmentManager as a parameter
    private var sellersList: ArrayList<Seller>
) : RecyclerView.Adapter<SellersAdapterCard.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SellersAdapterCard.MyViewHolder {
        val binding =
            RowSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: SellersAdapterCard.MyViewHolder, position: Int) {
        val seller = sellersList[position]
        holder.bind(seller)
    }

    override fun getItemCount(): Int {
        return sellersList.size
    }

    inner class MyViewHolder(var binding: RowSellerBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            binding.cardView.isClickable = true
            binding.cardView.setOnClickListener(this)
        }

        fun bind(seller: Seller) {
            binding.apply {
                txtServiceName.text = seller.service
                loadDetails(seller.userId)
                cardView.setCardBackgroundColor(Color.parseColor(Constants().getRandomHexColor()))
            }
        }

        private fun loadDetails(userId: String?) {
            val query: Query =
                Constants().getDatabaseReference().child("users").orderByChild("id").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userDetail in snapshot.children) {
                            val user = userDetail.getValue(User::class.java)

                            val name = "${user?.firstname} ${user?.lastname}"
                            binding.txtName.text = name


                            if (user!!.image.isNotEmpty()) {
                                Picasso.get().load(user.image).into(binding.imgProfile)
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


        override fun onClick(p0: View?) {
            val seller = sellersList[adapterPosition]
            val intent = Intent(context, UserDetailsactivity::class.java)
            intent.putExtra("id", seller.id)
            intent.putExtra("userId", seller.userId)
            context.startActivity(intent)

        }

    }


}