package com.ndt.bookonline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.databinding.ActivityDetailPdfBinding

class DetailPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPdfBinding

    private var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailPdfBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //get book id  tu intent
        bookId = intent.getStringExtra("bookId")!!

        loadBookDetail()

        //luot xem tu tang moi lan load
        MyApplication.incrementBookViewCount(bookId)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnReadBook.setOnClickListener {
            val intent = Intent(this, ViewPdfActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)

        }
    }

    private fun incrementBookViewCount(bookId: String) {

    }

    private fun loadBookDetail() {
        //Books > bookId > Detail
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val dowloadsCount = "${snapshot.child("dowloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load the loai
                    MyApplication.loadCategory(categoryId, binding.tvCategory)

                    //load trang, anh
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$url",
                        "$title",
                        binding.pdfView,
                        binding.progressBar,
                        binding.tvPages
                    )

                    //load pdf size
                    MyApplication.loadPdfSize("$url", "$title", binding.tvSize)

                    //set data
                    binding.tvTitle.text = title
                    binding.tvDescription.text = description
                    binding.tvView.text = viewsCount
                    binding.tvDowload.text = dowloadsCount
                    binding.tvDate.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}