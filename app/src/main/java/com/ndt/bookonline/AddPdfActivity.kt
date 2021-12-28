package com.ndt.bookonline

import android.app.Application
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ndt.bookonline.databinding.ActivityAddPdfBinding
import com.ndt.bookonline.databinding.ActivityCategoryAddBinding
import com.ndt.bookonline.databinding.ActivityRegisterBinding
import com.ndt.bookonline.model.Category

class AddPdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPdfBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<Category>


    //uri khi chọn pdf
    private var pdfUri: Uri? = null

    //TAG
    //private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddPdfBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPDFCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ..")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.tvCategory.setOnClickListener {
            categoryPickDialog()
        }

        binding.imageBtnAttachPDF.setOnClickListener {
            pdfPickIntent()
        }

        binding.btnSubmit.setOnClickListener {
            validateData()
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //Log.d(TAG, "validateData: validating data")
        //get data
        title = binding.edtTitle.text.toString().trim()
        description = binding.edtDescription.text.toString().trim()
        category = binding.tvCategory.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Nhập tiêu đề", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Nhập mô tả", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Chọn loại sách", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Chọn PDF", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }
        private fun uploadPdfToStorage() {
            //Log.d(TAG, "uploadPdfToStorage: uploading to storage...")
            progressDialog.setMessage("Tải lên PDF...")
            progressDialog.show()

            //tmestamp
            val timestamp = System.currentTimeMillis()

            //đường dẫn của pdf trong bộ nhớ firebase
            val filePathAndName = "Books/$timestamp"

            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(pdfUri!!)
                .addOnSuccessListener { taskSnapshot ->
                   // Log.d(TAG, "uploadPdfToStorage: PDF upload")
                    //lấy url của pdf đã tải lên
                    val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedPdfUrl = "${uriTask.result}"
                    uploadedPdfInfoToDb(uploadedPdfUrl, timestamp)
                }.addOnFailureListener {
                    //Log.d(TAG, "uploadPdfToStorage: thất bại")
                    progressDialog.dismiss()
                    Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show()
                }
        }

        private fun uploadedPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
            //upload pdf info to firebase db
          //  Log.d(TAG, "uploadPdfInfoToDb: Uploading to db")
            progressDialog.setMessage("Đang tải lên thông tin pdf ..")

            val uid = firebaseAuth.uid
            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["uid"] = "$uid"
            hashMap["id"] = "$timestamp"
            hashMap["title"] = "$title"
            hashMap["description"] = "$description"
            hashMap["categoryId"] = "$selectedCategoryId"
            hashMap["url"] = "$uploadedPdfUrl"
            hashMap["timestamp"] = timestamp
            hashMap["viewsCount"] = 0
            hashMap["dowloadsCount"] = 0

            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
               // Log.d(TAG, "uploadedPdfInfoToDb: upload to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Tải lên thành công", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }.addOnFailureListener {
                //Log.d(TAG, "uploadedPdfInfoToDb: thất bại")
                progressDialog.dismiss()
                Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show()
            }
        }

        private fun loadPDFCategories() {
            //Log.d(TAG, "loadPdfCategories: Loading pdf categories")
            categoryArrayList = ArrayList()

            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //xóa hết list sau khi add data
                    categoryArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(Category::class.java)
                        //add vào arraylist
                        categoryArrayList.add(model!!)
                        //Log.d(TAG, "onDataChange: ${model.category}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        private var selectedCategoryId = ""
        private var selectedCategoryTitle = ""

        private fun categoryPickDialog() {
           // Log.d(TAG, "categoryPickDialog: show pdf khi chọn")
            //lấy chuỗi các danh mục từ danh sách arraylist
            val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
            for (i in categoryArrayList.indices) {  //0..2
                categoriesArray[i] = categoryArrayList[i].category
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Chọn loại sách").setItems(categoriesArray) { dialog, which ->
                //xử lý click vào item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                //set category to tv
                binding.tvCategory.text = selectedCategoryTitle
               //Log.d(TAG, "categoryPickDialog: Selected Category Id: $selectedCategoryId")
               //Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }.show()
        }

        private fun pdfPickIntent() {
          //  Log.d(TAG, "pdfPickIntent: starting pdf pick intent ")

            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            pdfActivityResultLaucher.launch(intent)
        }

        val pdfActivityResultLaucher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == RESULT_OK) {
                    //Log.d(TAG, "PDF Picked ")
                    pdfUri = result.data!!.data
                } else {
                    //Log.d(TAG, "PDF Picked Canceled ")
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }