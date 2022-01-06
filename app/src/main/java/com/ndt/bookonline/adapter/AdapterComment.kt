package com.ndt.bookonline.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.R
import com.ndt.bookonline.databinding.ItemCommentBinding
import com.ndt.bookonline.model.Comment

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment> {
    val context: Context

    val commentArrayList: ArrayList<Comment>

    private lateinit var binding: ItemCommentBinding

    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<Comment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderComment(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile = binding.ivProfile
        val tvName = binding.tvName
        val tvDate = binding.tvDate
        val tvComment = binding.tvComment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        holder.tvDate.text = date
        holder.tvComment.text = comment

        loadUserDetails(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: Comment, holder: AdapterComment.HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Xóa bình luận").setMessage("Bạn có chắc chắn muốn xóa ?")
            .setPositiveButton("XÓA") { d, e ->

                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                    }

            }.setNegativeButton("THOÁT") { d, e ->
                d.dismiss()
            }.show()
    }

    private fun loadUserDetails(model: Comment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    holder.tvName.text = name
                    try {
                        Glide.with(context).load(profileImage).placeholder(R.drawable.ic_person)
                            .into(holder.ivProfile)
                    } catch (e: Exception) {
                        holder.ivProfile.setImageResource(R.drawable.ic_person)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int = commentArrayList.size
}