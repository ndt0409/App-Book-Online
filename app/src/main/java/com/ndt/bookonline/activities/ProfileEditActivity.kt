package com.ndt.bookonline.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.R
import com.ndt.bookonline.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadUserInfo()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivProfile.setOnClickListener {
            showImageAttachMenu()
        }

        binding.btnUpdate.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private fun validateData() {
        name = binding.edtName.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Nhập tên", Toast.LENGTH_SHORT).show()
        } else {
            //tên đã nhập
            if (imageUri == null) {
                updateProfile("")
            } else {
                uploadImage()
            }
        }

    }

    private fun uploadImage() {
        progressDialog.setMessage("đang tải ảnh lên..")
        progressDialog.show()

        //đường dẫn image và name, sử dụng uid để thay thế trước đó
        val filePathAndName = "ProfileImages/" + firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "tải ảnh lên thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadImageUrl: String) {
        progressDialog.setMessage("Cập nhập thông tin")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri != null) {
            hashMap["profileImage"] = uploadImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Cập nhập thông tin cá nhân thành công", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "cập nhập thông tin thất bại", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserInfo() {
        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //set data
                    binding.edtName.setText(name)

                    //set image
                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.ivProfile)
                    } catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    //hiển thị menu đính kèm ảnh
    private fun showImageAttachMenu() {
        //show popup menu với option Camera, thư viện chọn ảnh

        val popupMenu = PopupMenu(this, binding.ivProfile)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Chụp ảnh")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Thư viện")
        popupMenu.show()

        //click vào item
        popupMenu.setOnMenuItemClickListener { item ->
            //get id của item đã click
            val id = item.itemId
            if (id == 0) {
                pickImageCamera()
            } else if (id == 1) {
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    //sử dụng để xử lý kết quả intent máy ảnh (cách mới thay thế cho startActivityForResult)
    private val cameraActivityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    //imageUri = data!!.data

                    //set imageview
                    binding.ivProfile.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            })
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                //set imageview
                binding.ivProfile.setImageURI(imageUri)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        })
}