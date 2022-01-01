package com.ndt.bookonline.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//khoi tao firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //thanh load
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //xu ly nut back, quay ve man hinh chinh
        binding.tvNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            validateData()
        }

    }

    private var password = ""
    private var email = ""
    private fun validateData() {
        //input du lieu
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPass.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng email", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        //login - firebase auth

        //load
        progressDialog.setMessage("Thiết lập tài khoản...")
        progressDialog.show()
        //tao user trong firebase auth
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            checkUser()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Đăng nhập thất bại, vui lòng kiểm tra lại đường truyền", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkUser() {
        //neu la ad chuyen dashbroad ad va ngc lai la user
        progressDialog.setMessage("Checking...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()

                //get loai user/admin
                val userType = snapshot.child("userType").value
                if (userType == "user") {
                    startActivity(Intent(this@LoginActivity, DashbroadUserActivity::class.java))
                    finish()
                } else if (userType == "admin"){
                    startActivity(Intent(this@LoginActivity, DashbroadAdminActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
