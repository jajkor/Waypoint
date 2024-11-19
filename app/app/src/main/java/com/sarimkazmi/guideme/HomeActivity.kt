package com.sarimkazmi.guideme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.waypoint.MainActivity
import com.sarimkazmi.guideme.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardRydal.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cardLares.setOnClickListener {
            startActivity(Intent(this, LaresActivity::class.java))
        }

        binding.cardWoodland.setOnClickListener {
            startActivity(Intent(this, WoodlandActivity::class.java))
        }

        binding.cardSutherland.setOnClickListener {
            startActivity(Intent(this, SutherlandActivity::class.java))
        }
    }
}
