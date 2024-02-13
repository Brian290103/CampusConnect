package com.example.campusconnect.admin.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.campusconnect.databinding.ActivityAdminDashboardBinding
import org.eazegraph.lib.models.BarModel
import org.eazegraph.lib.models.PieModel


class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)

        with(binding){

            toolbar.toolbar.setNavigationOnClickListener { finish() }
            toolbar.toolbar.title="Admin Dashboard"

            barchart.addBar(BarModel(2.3f, 0xFF123456.toInt()))
            barchart.addBar(BarModel(2f, 0xFF343456.toInt()))
            barchart.addBar(BarModel(3.3f, 0xFF563456.toInt()))
            barchart.addBar(BarModel(1.1f, 0xFF873F56.toInt()))
            barchart.addBar(BarModel(2.7f, 0xFF56B7F1.toInt()))
            barchart.addBar(BarModel(2f, 0xFF343456.toInt()))
            barchart.addBar(BarModel(0.4f, 0xFF1FF4AC.toInt()))
            barchart.addBar(BarModel(4f, 0xFF1BA4E6.toInt()))

            barchart.startAnimation()

            manageUsers.setOnClickListener{
                startActivity(Intent(this@AdminDashboardActivity,ManageUsersActivity::class.java))
            }

            servicesPiechart.addPieSlice(PieModel("Freetime", 15f, Color.parseColor("#FE6DA8")))
            servicesPiechart.addPieSlice(PieModel("Sleep", 25f, Color.parseColor("#56B7F1")))
            servicesPiechart.addPieSlice(PieModel("Work", 35f, Color.parseColor("#CDA67F")))
            servicesPiechart.addPieSlice(PieModel("Eating", 9f, Color.parseColor("#FED70E")))

            servicesPiechart.startAnimation()

            manageServiceCategories.setOnClickListener{
                startActivity(Intent(this@AdminDashboardActivity,ManageServicesActivity::class.java))
            }
        }
    }
}