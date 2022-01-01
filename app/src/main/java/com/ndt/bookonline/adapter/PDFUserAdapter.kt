package com.ndt.bookonline.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ndt.bookonline.activities.DetailPdfActivity
import com.ndt.bookonline.filters.FilterPdfUser
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.databinding.ItemPdfUserBinding
import com.ndt.bookonline.model.PDF

class PDFUserAdapter : RecyclerView.Adapter<PDFUserAdapter.HolderPdfUser>, Filterable {

    private var context: Context

    var pdfArrayList: ArrayList<PDF>

    var filterList: ArrayList<PDF>

    private var filter: FilterPdfUser? = null

    private lateinit var binding: ItemPdfUserBinding

    constructor(context: Context, pdfArrayList: ArrayList<PDF>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = ItemPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = date

        //load category
        MyApplication.loadCategory(categoryId, holder.tvCategory)

        //không cần số trang ở đây, đặt null cho số trang
        MyApplication.loadPdfFromUrlSinglePage(
            url,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(url, title, holder.tvSize)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPdfActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pdfArrayList.size

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var tvTitle = binding.tvTitle
        var tvDescription = binding.tvDescription
        var tvCategory = binding.tvCategory
        var tvSize = binding.tvSize
        var tvDate = binding.tvDate
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
        return filter as FilterPdfUser
    }


}