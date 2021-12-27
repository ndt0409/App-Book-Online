package com.ndt.bookonline

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import java.util.*

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, PdfTitle: String, tvSize: TextView) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener {
                val bytes = it.sizeBytes.toDouble()
                //chuyển byte sang KB/MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb > 1) {
                    tvSize.text = "${String.format("%.2f", mb)} + MB"
                } else if (kb >= 1) {
                    tvSize.text = "${String.format("%.2f", kb)} + KB"
                } else {
                    tvSize.text = "${String.format("%.2f", bytes)} + bytes"
                }
            }.addOnFailureListener {

            }
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            tvPages: TextView?
        ) {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->

                pdfView.fromBytes(bytes)
                    .pages(0) //trang đầu tiên
                    .spacing(0)
                    .swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError {
                        progressBar.visibility = View.INVISIBLE
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                    }
                    .onLoad { nbPages ->
                        //pdf được tải, có thể đặt số lượng trang, hình thu nhỏ pdf
                        progressBar.visibility = View.INVISIBLE

                        //nếu tvPages param không phải là null thì hãy đặt số trang
                        if (tvPages != null) {
                            tvPages.text = "$nbPages"
                        }
                    }.load()
            }.addOnFailureListener {
                //Log.d("ndt", "failed")
            }
        }

        fun loadCategory(categoryId: String, tvCategory: TextView) {
            //load category dùng category id từ firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val category: String = "${snapshot.child("category").value}"

                    tvCategory.text = category
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Vui lòng chờ..")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Delete successfully", Toast.LENGTH_SHORT).show()

                        }
                        .addOnFailureListener{
                            progressDialog.dismiss()
                            Toast.makeText(context, "Failed db", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener{
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed storage", Toast.LENGTH_SHORT).show()
                }

        }
    }
}
