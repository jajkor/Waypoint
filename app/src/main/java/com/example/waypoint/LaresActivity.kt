package com.example.waypoint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.waypoint.databinding.ActivityLaresBinding

class LaresActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Lares"
            setDisplayHomeAsUpEnabled(true) // Show back button
        }

        binding.toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))
        binding.toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white, theme))
    }

    // Handle back button click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
