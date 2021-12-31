package com.ndt.bookonline

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ndt.bookonline.databinding.ActivityDetailPdfBinding
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.Manifest

class DetailPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPdfBinding

    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    //get from intent
    private var bookId = ""

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailPdfBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //get book id  tu intent
        bookId = intent.getStringExtra("bookId")!!

        //init progressBar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ..")
        progressDialog.setCanceledOnTouchOutside(false)

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

        binding.btnDowloadBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("ndt", "permission granted")
            } else {
                Log.d("ndt", "permission denied")
                requestStorePermissonLaucher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestStorePermissonLaucher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("ndt", "permission granted")
                dowloadBook()
            } else {
                Log.d("ndt", "permission denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun dowloadBook() {
        progressDialog.setTitle("Dowload book..")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveToDowloadsFolder(bytes)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Dowload that bai kiem tra lai mang", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveToDowloadsFolder(bytes: ByteArray?) {
        val nameWithExtension = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() //tao folder neu khong ton tai

            val filePath = downloadsFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Đã lưu vào folder download", Toast.LENGTH_SHORT)
                .show()
            progressDialog.dismiss()
            incrementDownloadCount()
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "That bai", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun incrementDownloadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    val newDownloadsCount: Long = downloadsCount.toLong() + 1

                    //lay du lieu tu db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["dowloadsCount"]

                    //update len db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener{

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
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
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load the loai
                    MyApplication.loadCategory(categoryId, binding.tvCategory)

                    //load trang, anh
                    MyApplication.loadPdfFromUrlSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.tvPages
                    )

                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.tvSize)

                    //set data
                    binding.tvTitle.text = bookTitle
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