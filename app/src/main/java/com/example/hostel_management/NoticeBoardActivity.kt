package com.example.hostel_management

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoticeBoardActivity : AppCompatActivity() {

    private lateinit var rvNotices: RecyclerView
    private lateinit var fabAddNotice: FloatingActionButton
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_board)

        rvNotices = findViewById(R.id.rvNotices)
        fabAddNotice = findViewById(R.id.fabAddNotice)
        firebaseHelper = FirebaseHelper()

        // Hide FAB for students so not allowed to add notices
        fabAddNotice.visibility = View.GONE

        // Setup adapter with isAdmin = false (hides delete buttons)
        noticeAdapter = NoticeAdapter(noticeList, onDelete = { }, isAdmin = false)

        rvNotices.layoutManager = LinearLayoutManager(this)
        rvNotices.adapter = noticeAdapter

        // Removal of FAB click listener since students cannot add
        // fabAddNotice.setOnClickListener { ... } // Removed

        loadNotices()
    }

    private fun loadNotices() {
        lifecycleScope.launch {
            val notices = withContext(Dispatchers.IO) {
                firebaseHelper.getAllNotices()
            }
            noticeList.clear()
            noticeList.addAll(notices)
            noticeAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotices()
    }
}
