package com.ndt.bookonline.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ndt.bookonline.Constants
import com.ndt.bookonline.databinding.ActivityViewPdfBinding

class ViewPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewPdfBinding

    var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewPdfBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //get bookId từ intent để load sách từ firebase
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //get book url
                    val urlPDF = snapshot.child("url").value
                    Log.d("ndt", "onDataChange: url pdf: $urlPDF")

                    loadBookFromUrl("$urlPDF")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookFromUrl(urlPDF: String) {
        //Log.d("ndt", "lay pdf từ firebaseStorage bằng url")
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(urlPDF)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.viewPdf.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        //set tổng số trang trên subtitle
                        val currentPage = page + 1
                        binding.tvSubTitle.text = "$currentPage/$pageCount"
                        Log.d("ndt", "$currentPage/$pageCount")
                    }
                    .onError {
                        Log.d("ndt", "${it.message}")
                    }
                    .onPageError { page, t ->
                        Log.d("ndt", "${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {

            }
    }
}