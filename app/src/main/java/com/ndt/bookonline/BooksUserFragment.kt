package com.ndt.bookonline

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.adapter.PDFUserAdapter
import com.ndt.bookonline.databinding.FragmentBooksUserBinding
import com.ndt.bookonline.model.PDF
import java.lang.Exception

class BooksUserFragment : Fragment {

    private lateinit var binding: FragmentBooksUserBinding

    companion object {
        fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()
            //put data qua voi bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<PDF>
    private lateinit var adapterPdfUser: PDFUserAdapter

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //lấy các đối số đã truyền trong phương thức newInstance
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        // tải pdf theo danh mục, newInstance load từng loại pdf
        if (category == "Tất cả") {
            //load all book
            loadAllBook()
        } else if (category == "Xem nhiều nhất") {
            loadMostDownloadBook("viewsCount")
        } else if (category == "Tải nhiều nhất") {
            loadMostDownloadBook("dowloadsCount")
        } else {
            //load mục đã chọn
            loadCategoryBook()
        }
        //search
        binding.edtSearch.addTextChangedListener {
            object : TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        adapterPdfUser.filter.filter(s)
                    } catch (e: Exception) {

                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }
            }
        }

        return binding.root
    }

    private fun loadAllBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(PDF::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList)
                binding.rvBooks.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadMostDownloadBook(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10) //load 10 cái download nhiều nhất
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(PDF::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList)
                binding.rvBooks.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategoryBook() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(PDF::class.java)

                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    adapterPdfUser = PDFUserAdapter(context!!, pdfArrayList)
                    binding.rvBooks.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}