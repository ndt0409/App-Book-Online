package com.ndt.bookonline

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.ndt.bookonline.databinding.ActivityDashbroadUserBinding

class DashbroadUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashbroadUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashbroadUserBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        // get user current
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.tvSubTitle.text = "Not logged in"
        } else {
            val email = firebaseUser.email
            //set to tv of toolbar
            binding.tvSubTitle.text = email
        }
    }
}