package com.ndt.bookonline.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.ndt.bookonline.filters.FilterCategory
import com.ndt.bookonline.activities.ListPDFAdminActivity
import com.ndt.bookonline.databinding.ItemCategoryBinding
import com.ndt.bookonline.model.Category

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.HolderCategory>, Filterable {

    private val context: Context

    var categoryArrayList: ArrayList<Category>

    private var filterList: ArrayList<Category>

    private var filter: FilterCategory? = null

    private lateinit var binding: ItemCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<Category>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.tvCategory.text = category
        holder.btnDelete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Xóa").setMessage("bạn có chắc muốn xóa?")
                .setPositiveButton("Đồng ý") { a, d ->
                    Toast.makeText(context, "đang xóa..", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }.setNegativeButton("Thoát") { a, d ->
                    a.dismiss()
                }
                .show()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ListPDFAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: Category, holder: HolderCategory) {
        //lấy id để xóa
        val id = model.id
        //firebaseDB > categories > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id).removeValue().addOnSuccessListener {
            Toast.makeText(context, "đang xóa..", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Xóa thất bại...", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int = categoryArrayList.size

    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCategory: TextView = binding.tvCategory
        var btnDelete: ImageButton = binding.btnDelete
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}