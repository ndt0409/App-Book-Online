package com.ndt.bookonline

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ndt.bookonline.databinding.ActivityRegisterBinding
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //khoi tao firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //thanh load
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng chờ...")
        progressDialog.setCanceledOnTouchOutside(false)

        //xu ly nut back, quay ve man hinh chinh
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnRegister.setOnClickListener {

            //tao tai khoan - firebase auth
            //luu thong tin ng dung - firebase realtime database
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        //input du lieu
        name = binding.edtName.text.toString().trim()
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPass.text.toString().trim()
        val cPassword = binding.edtConfirmPass.text.toString().trim()
        //validate data
        if (name.isEmpty()) {
            //empty name
            Toast.makeText(this, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng email", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập lại mật khẩu", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Mật khẩu không giống nhau, nhập lại", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //tao tai khoan - firebase auth
        progressDialog.setMessage("Tạo tài khoản mới...")
        progressDialog.show()
        //tao user trong firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            updateUserInfor()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Thất bại, vui lòng kiểm tra lại đường truyền", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUserInfor() {
        //luu thong tin ng dung realtime - firebase realtime
        progressDialog.setMessage("Lưu thông tin tài khoản")
        //timestamp
        val timestamp = System.currentTimeMillis() //khoảng thời gian tính đến mili s
        //lấy uid người dùng hiện tại vì người dùng đã được đăng ký để có thể lấy ngay
        val uid = firebaseAuth.uid

        //setup dữ liệu để thêm vào db
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = uid!!
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user" //admin hoac user
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid).setValue(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Tạo tài khoản..", Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(this@RegisterActivity, DashbroadUserActivity::class.java))
            finish()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT)
                .show()
        }
    }
}