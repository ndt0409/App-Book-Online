package com.ndt.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.adapter.PDFAdminAdapter
import com.ndt.bookonline.databinding.ActivityListPdfadminBinding
import com.ndt.bookonline.model.PDF

class ListPDFAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListPdfadminBinding

    private var categoryId = ""
    private var category = ""

    private lateinit var pdfArrayList: ArrayList<PDF>

    private lateinit var pdfAdminAdapter: PDFAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListPdfadminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.tvSubTitle.text = category

        //load pdf category
        loadPdfList()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //search
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //filter data
                try {
                    pdfAdminAdapter.filter!!.filter(s)
                } catch (e: Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadPdfList() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(PDF::class.java)
                        //add to list
                        if (model != null) {
                            pdfArrayList.add(model)
                        }
                    }
                    pdfAdminAdapter = PDFAdminAdapter(this@ListPDFAdminActivity, pdfArrayList)
                    binding.rvBooks.adapter = pdfAdminAdapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}