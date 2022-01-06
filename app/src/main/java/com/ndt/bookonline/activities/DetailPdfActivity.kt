package com.ndt.bookonline.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ndt.bookonline.Constants
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.R
import com.ndt.bookonline.adapter.AdapterComment
import com.ndt.bookonline.databinding.ActivityDetailPdfBinding
import com.ndt.bookonline.databinding.DialogCommentAddBinding
import com.ndt.bookonline.model.Comment
import java.io.FileOutputStream

class DetailPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPdfBinding

    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    //
    //Giữ giá trị boolean false/true để biết user hiện tại có phải là danh sách yêu thích hay không
    private var isInMyFavorite = false

    //get from intent
    private var bookId = ""

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var commentArrayList: ArrayList<Comment>

    private lateinit var adapterComment: AdapterComment

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

        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        //luot xem tu tang moi lan load
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetail()
        showComments()

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
                dowloadBook()
            } else {
                Log.d("ndt", "permission denied")
                requestStorePermissonLaucher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.btnFavorite.setOnClickListener {
            //chỉ add khi đã đăng nhập
            //check user đã đăng nhập hay chưa
            if (firebaseAuth.currentUser == null) {
                //chưa đăng nhập không thể add favorite
                Toast.makeText(this, "Bạn không đăng nhập", Toast.LENGTH_SHORT).show()
            } else {
                //user đã đn
                if (isInMyFavorite) {
                    MyApplication.removeFromFavorite(this, bookId)
                } else {
                    addToFavorite()
                }
            }
        }
        binding.btnAddComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(
                    this,
                    "Bạn chưa đăng nhập, vui lòng đăng nhập để có thể dùng chức năng này",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                addCommentDialog()
            }
        }
    }

    private fun showComments() {
        commentArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Comment::class.java)
                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@DetailPdfActivity, commentArrayList)

                    binding.rvComment.adapter = adapterComment
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var comment = ""
    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //tạo và show thông báo
        val alertDialog = builder.create()
        alertDialog.show()

        //click quay lại, tắt dialog
        commentAddBinding.btnBack.setOnClickListener {
            alertDialog.dismiss()
        }
        commentAddBinding.btnSubmit.setOnClickListener {
            comment = commentAddBinding.edtComment.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(
                    this,
                    "Bạn chưa nêu cảm nghĩ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Đang tải bình luận lên")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //books > bookId > Comments > commentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Vừa thêm 1 bình luận", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Thêm bình luận thất bại",
                    Toast.LENGTH_SHORT
                ).show()
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
            Toast.makeText(this, "Thất bại, vui lòng kiểm tra lại 3G/Wifi", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun incrementDownloadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("dowloadsCount").value}"

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    val newDownloadsCount: Long = downloadsCount.toLong() + 1

                    //lay du lieu tu db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["dowloadsCount"] = newDownloadsCount

                    //update len db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener {

                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
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

    private fun checkIsFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite,
                            0,
                            0
                        )
                        binding.btnFavorite.text = "Xóa khỏi danh sách yêu thích"
                    } else {
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border,
                            0,
                            0
                        )
                        binding.btnFavorite.text = "Thêm vào danh sách yêu thích"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        val timestamp = System.currentTimeMillis()

        //thiết lập dữ liệu để thêm vào db
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //lưu vào db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show()
            }
    }
}