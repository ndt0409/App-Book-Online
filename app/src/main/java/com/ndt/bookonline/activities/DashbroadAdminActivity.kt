package com.ndt.bookonline.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.adapter.CategoryAdapter
import com.ndt.bookonline.databinding.ActivityDashbroadAdminBinding
import com.ndt.bookonline.model.Category

class DashbroadAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashbroadAdminBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<Category>

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashbroadAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategory()
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    categoryAdapter.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        binding.addPDF.setOnClickListener {
            startActivity(Intent(this, AddPdfActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadCategory() {
        categoryArrayList = ArrayList()
        //get all categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Category::class.java)
                    categoryArrayList.add(model!!)
                }
                categoryAdapter = CategoryAdapter(this@DashbroadAdminActivity, categoryArrayList)
                binding.rvCategory.adapter = categoryAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        // get user current
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            //set to tv of toolbar
            binding.tvSubTitle.text = email
        }
    }
}