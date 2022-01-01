package com.ndt.bookonline.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ndt.bookonline.BooksUserFragment
import com.ndt.bookonline.databinding.ActivityDashbroadUserBinding
import com.ndt.bookonline.model.Category
import kotlin.collections.ArrayList

class DashbroadUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashbroadUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<Category>

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashbroadUserBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )

        categoryArrayList = ArrayList()

        //load category từ db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = Category("01", "Tất cả", 1, "")
                val modelMostViewer = Category("01", "Xem nhiều nhất", 1, "")
                val modelMostDownload = Category("01", "Tải nhiều nhất", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewer)
                categoryArrayList.add(modelMostDownload)

                //add vào viewpager
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}", "${modelAll.category}", "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewer.id}",
                        "${modelMostViewer.category}",
                        "${modelMostViewer.uid}"
                    ), modelMostViewer.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownload.id}",
                        "${modelMostDownload.category}",
                        "${modelMostDownload.uid}"
                    ), modelMostDownload.category
                )
                viewPagerAdapter.notifyDataSetChanged()

                //load từ firebasedb
                for (ds in snapshot.children){
                    val model=  ds.getValue(Category::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewpager
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}", "${model.category}", "${model.uid}"
                        ), model.category
                    )
                    //load lại list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        //giữ danh sách fragment, newInstance cùng một fragment cho mỗi danh mục
        private val fragmentList: ArrayList<BooksUserFragment> = ArrayList()

        //danh sách tiêu đề thanh tab
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: BooksUserFragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    //activity này được mở khi đn hoặc chưa đn => hãy ẩn btnlogout và btnProfile khi người dùng không đăng nhập
    private fun checkUser() {
        // get user current
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.tvSubTitle.text = "Chưa đăng nhập"

            //ẩn profile, logout
            binding.btnProfile.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
        } else {
            val email = firebaseUser.email
            //set to tv of toolbar
            binding.tvSubTitle.text = email

            //ẩn profile, logout
            binding.btnProfile.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE
        }
    }
}