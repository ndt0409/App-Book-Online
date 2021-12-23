package com.ndt.bookonline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndt.bookonline.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {

        }
        binding.btnSkip.setOnClickListener {

        }
    }
}