package com.ndt.bookonline

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ndt.bookonline.databinding.ActivityCategoryAddBinding

class CategoryAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            validateData()
        }

    }

    private var category = ""
    private fun validateData() {
        //get data
        category = binding.edtCategory.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Nhập loại sách..", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hashMap =
            hashMapOf<String, Any>()//tham số thứ 2 là Any vì giá trị có thể thuộc bất kỳ loại nào
        hashMap["id"] =
            "$timestamp" //đặt trong dấu ngoặc kép vì thời gian ở dạng double, cần trong chuỗi ký tự cho id
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            Toast.makeText(this, "Thêm thành công ", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show()
        }
    }
}