package com.ndt.bookonline.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.activities.DetailPdfActivity
import com.ndt.bookonline.databinding.ItemPdfFavoriteBinding
import com.ndt.bookonline.model.PDF

class AdapterPdfFavorite : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private val context: Context

    private var bookArrayList: ArrayList<PDF>

    private lateinit var binding: ItemPdfFavoriteBinding

    constructor(context: Context, bookArrayList: ArrayList<PDF>) {
        this.context = context
        this.bookArrayList = bookArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        binding = ItemPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        val model = bookArrayList[position]
        loadBookDetail(model, holder)

        //xử lý mở detail pdf, chuyển bookid để load detail
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPdfActivity::class.java)
            intent.putExtra("bookId", model.id) //pass book id not category id
            context.startActivity(intent)
        }

        holder.btnRemoveFavorite.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadBookDetail(model: PDF, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val bookId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("dowloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewCount = viewsCount.toLong()
                    model.dowloadsCount = downloadsCount.toLong()

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
                        null
                    )

                    //load pdf size
                    MyApplication.loadPdfSize("$url", "$title", binding.tvSize)

                    //set data
                    binding.tvTitle.text = title
                    binding.tvDescription.text = description
                    binding.tvDate.text = date
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun getItemCount(): Int = bookArrayList.size

    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var viewPdf = binding.pdfView
        var processBar = binding.progressBar
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvCategory = binding.tvCategory
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val btnRemoveFavorite = binding.btnRemoveFavorite
    }


}