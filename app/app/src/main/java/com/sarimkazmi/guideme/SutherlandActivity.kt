package com.sarimkazmi.guideme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sarimkazmi.guideme.databinding.ActivitySutherlandBinding

class SutherlandActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySutherlandBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySutherlandBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Sutherland"
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