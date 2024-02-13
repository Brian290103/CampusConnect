package com.example.campusconnect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.campusconnect.R
import com.example.campusconnect.databinding.ActivityMainBinding
import com.example.campusconnect.fragments.HomeFragment
import com.example.campusconnect.fragments.InboxFragment
import com.example.campusconnect.fragments.OrdersFragment
import com.example.campusconnect.fragments.ProfileFragment
import com.example.campusconnect.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var onResumeFragment: Fragment = HomeFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        with(binding){
            bottomNavigation.itemIconTintList=null
            toolbar.setNavigationOnClickListener {
                drawerLayout.open()
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                // Handle menu item selected
                menuItem.isChecked = true
                drawerLayout.close()
                true
            }

            navigationView.setNavigationItemSelectedListener { menuItem ->
                // Handle menu item selected
                when (menuItem.itemId) {
                    R.id.mn_home -> {
                        loadFragment(HomeFragment())
                        bottomNavigation.selectedItemId = R.id.mn_home
                    }

                    R.id.mn_inbox -> {
                        loadFragment(InboxFragment())
                        bottomNavigation.selectedItemId = R.id.mn_inbox
                    }

                    R.id.mn_search -> {
                        loadFragment(SearchFragment())
                        bottomNavigation.selectedItemId = R.id.mn_search
                    }
                    R.id.mn_orders -> {
                        loadFragment(OrdersFragment())
                        bottomNavigation.selectedItemId = R.id.mn_orders
                    }
                    R.id.mn_profile -> {
                        loadFragment(ProfileFragment())
                        bottomNavigation.selectedItemId = R.id.mn_profile
                    }
                }
                menuItem.isChecked = true
                drawerLayout.close()
                true
            }

            navigationView.itemIconTintList=null

            bottomNavigation.setOnItemSelectedListener { item ->
                when(item.itemId) {
                    R.id.mn_home -> {
                        loadFragment(HomeFragment())
                        onResumeFragment=HomeFragment()
                        navigationView.setCheckedItem(R.id.mn_home)
                        true
                    }
                    R.id.mn_inbox -> {
                        loadFragment(InboxFragment())
                        navigationView.setCheckedItem(R.id.mn_inbox)
                        true
                    }
                    R.id.mn_search -> {
                        loadFragment(SearchFragment())
                        navigationView.setCheckedItem(R.id.mn_search)
                        true
                    }
                    R.id.mn_orders -> {
                        loadFragment(OrdersFragment())
                        navigationView.setCheckedItem(R.id.mn_orders)
                        true
                    }
                    R.id.mn_profile -> {
                        loadFragment(ProfileFragment())
                        navigationView.setCheckedItem(R.id.mn_profile)
                        onResumeFragment=ProfileFragment()
                        true
                    }
                    else -> false
                }
            }

            // End of binding
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    //OverRidden Functions
    override fun onResume() {
        super.onResume()
        loadFragment(onResumeFragment)
        binding.navigationView.setCheckedItem(R.id.mn_home)
        when (onResumeFragment) {
            is HomeFragment -> binding.bottomNavigation.selectedItemId = R.id.mn_home
        }
    }


    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.commit()
    }
}