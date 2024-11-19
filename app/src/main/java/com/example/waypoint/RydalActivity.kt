package com.example.waypoint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.waypoint.databinding.ActivityRydalBinding

class RydalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRydalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRydalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Rydal"
            setDisplayHomeAsUpEnabled(true) // Show back button
        }

        // Set title and navigation icon color to white
        binding.toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))
        binding.toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white, theme))
    }

    // Handle back button click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
