package com.ndt.bookonline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.ndt.bookonline.FilterCategory
import com.ndt.bookonline.FilterPdfAdmin
import com.ndt.bookonline.MyApplication
import com.ndt.bookonline.databinding.ItemPdfAdminBinding
import com.ndt.bookonline.model.PDF

class PDFAdminAdapter : RecyclerView.Adapter<PDFAdminAdapter.HolderPdfAdmin>, Filterable {

    private var context: Context

    public var pdfArrayList: ArrayList<PDF>

    private var filterList: ArrayList<PDF>

    var filter: FilterPdfAdmin? = null

    private lateinit var binding: ItemPdfAdminBinding

    constructor(context: Context, pdfArrayList: ArrayList<PDF>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = ItemPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp sang dd/mm/yyyy
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = formattedDate

        //load category
        MyApplication.loadCategory(categoryId, holder.tvCategory)

        //không cần số trang ở đây, đặt null cho số trang
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.tvSize)
    }

    override fun getItemCount(): Int = pdfArrayList.size

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfView = binding.pdfView
        val progressBar = binding.pb
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvCategory = binding.tvCategory
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val btnMore = binding.btnMore
    }
}