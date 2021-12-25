package com.ndt.bookonline

import android.widget.Filter
import android.widget.Filterable
import com.ndt.bookonline.adapter.CategoryAdapter
import com.ndt.bookonline.model.Category


class FilterCategory : Filter {
    //các ds cần tìm kiếm
    private var filterList: ArrayList<Category>

    var adapterCategory: CategoryAdapter

    constructor(filterList: ArrayList<Category>, adapterCategory: CategoryAdapter) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val result = FilterResults()

        //giá trị k đc null và trống
        if (constraint != null && constraint.isNotEmpty()) {
            //thay đổi thành chữ hoa hoặc chữ thường để tránh phân biệt chữ hoa chữ thường
            constraint = constraint.toString().uppercase()
            val filterModel: ArrayList<Category> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].category.uppercase().contains(constraint)) {
                    //thêm vào danh sách đã lọc
                    filterModel.add(filterList[i])
                }
            }
            result.count = filterModel.size
            result.values = filterModel
        } else {
            result.count = filterList.size
            result.values = filterList
        }
        return result //bị quên hơi cay
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //lọc
        adapterCategory.categoryArrayList = results.values as ArrayList<Category>

        adapterCategory.notifyDataSetChanged()
    }
}