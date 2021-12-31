package com.ndt.bookonline

import android.widget.Filter
import com.ndt.bookonline.adapter.PDFAdminAdapter
import com.ndt.bookonline.adapter.PDFUserAdapter
import com.ndt.bookonline.model.PDF

class FilterPdfUser:Filter {
    var filterList: ArrayList<PDF>

    var adapterPdfUser: PDFUserAdapter

    constructor(filterList: ArrayList<PDF>, adapterPdfUser: PDFUserAdapter) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        //giá trị k đc null và trống
        if (constraint != null && constraint.isNotEmpty()) {
            //thay đổi thành chữ hoa hoặc chữ thường để tránh phân biệt chữ hoa chữ thường
            constraint = constraint.toString().lowercase()
            val filterModel = ArrayList<PDF>()
            for (i in filterList.indices) {
                if (filterList[i].title.lowercase().contains(constraint)) {
                    //thêm vào danh sách đã lọc
                    filterModel.add(filterList[i])
                }
            }
            results.count = filterModel.size
            results.values = filterModel
        } else {
            //giá trị được tìm kiếm là null hoặc rỗng, trả về tất cả dữ liệu
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        //lọc
        adapterPdfUser.pdfArrayList = results!!.values as ArrayList<PDF>

        adapterPdfUser.notifyDataSetChanged()
    }
}