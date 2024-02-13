package com.example.campusconnect.admin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusconnect.R
import com.example.campusconnect.admin.adapters.UserAdapter
import com.example.campusconnect.databinding.ActivityManageUsersBinding
import com.example.campusconnect.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class ManageUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageUsersBinding
    private var filterOption = "All Users"
    private lateinit var userAdapter: UserAdapter
    private lateinit var originalUsersList: ArrayList<User>

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar.toolbar)


        originalUsersList = ArrayList()
        userAdapter = UserAdapter(this, supportFragmentManager, originalUsersList)

        with(binding) {
            myToolbar.toolbar.setNavigationOnClickListener { finish() }
            myToolbar.toolbar.title = "Manage Users"

            filter.setOnClickListener { v: View ->
                showMenu(v, R.menu.popup_menu)
            }

            recyclerView.layoutManager = LinearLayoutManager(this@ManageUsersActivity)
            binding.recyclerView.adapter = userAdapter
        }
        getUsers("all")
    }

    private fun getUsers(filter: String) {
        val query: Query = databaseReference.orderByChild("role").equalTo("user")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressCircular.isVisible = false

                originalUsersList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)

                    if (filter == "all" || (filter.equals("sellers") && user?.seller == true)
                    ) {
                        user?.let { originalUsersList.add(it) }
                    }

                }

                userAdapter.notifyDataSetChanged()
//                for (user in originalUsersList) {
//                    Log.d(
//                        "UserDetails",
//                        "ID: ${user.id}, Name: ${user.firstname} ${user.lastname}, Email: ${user.email}"
//                    )
//                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageUsersActivity, "Failed to Fetch", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.pmn_all -> {
                    filterOption = "All Users"
                    binding.txtFilter.text = "All Users"
                    getUsers("all")
                    true
                }

                R.id.pmn_sellers -> {
                    filterOption = "Sellers"
                    binding.txtFilter.text = "Sellers"
                    getUsers("sellers")
                    true
                }

                else -> false
            }
        }

        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }

        popup.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchViewItem = menu.findItem(R.id.mn_search)
        val searchView = searchViewItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText!!)
                return true
            }
        })
        return true
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<User>()

        // running a for loop to compare elements.
        for (item in originalUsersList) {
            // checking if the entered string matched with any item of our recycler view.

            if (item.firstname.toLowerCase().contains(text) ||
                item.lastname.toLowerCase().contains(text) ||
                item.registrationNumber.toLowerCase().contains(text)
            ) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            userAdapter.filterList(filteredlist)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }



}
