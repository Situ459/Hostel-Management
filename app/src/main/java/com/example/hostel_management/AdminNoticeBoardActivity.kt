package com.example.hostel_management

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminNoticeBoardActivity : AppCompatActivity() {

    private lateinit var rvNotices: RecyclerView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnAddNotice: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var noticeAdapter: NoticeAdapter
    private val noticeList = mutableListOf<Notice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notice_board)

        rvNotices = findViewById(R.id.rvNotices)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnAddNotice = findViewById(R.id.btnAddNotice)
        progressBar = findViewById(R.id.progressBar)

        firebaseHelper = FirebaseHelper()

        noticeAdapter = NoticeAdapter(noticeList, onDelete = { notice ->
            deleteNotice(notice.noticeId)
        }, isAdmin = true)

        rvNotices.layoutManager = LinearLayoutManager(this)
        rvNotices.adapter = noticeAdapter

        btnAddNotice.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Enter both title and content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addNotice(title, content)
        }

        loadNotices()
    }

    private fun loadNotices() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val notices = withContext(Dispatchers.IO) {
                firebaseHelper.getAllNotices()
            }
            noticeList.clear()
            noticeList.addAll(notices)
            noticeAdapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }
    }

    private fun addNotice(title: String, content: String) {
        val userId = firebaseHelper.getCurrentUserId()
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val userDetails = withContext(Dispatchers.IO) { firebaseHelper.getUserDetails(userId) }
            val createdBy = userDetails?.name ?: "Admin"
            val result = withContext(Dispatchers.IO) {
                firebaseHelper.addNotice(title, content, createdBy)
            }
            progressBar.visibility = View.GONE
            if (result.isSuccess) {
                Toast.makeText(this@AdminNoticeBoardActivity, "Notice added", Toast.LENGTH_SHORT).show()
                etTitle.text.clear()
                etContent.text.clear()
                loadNotices()
            } else {
                Toast.makeText(this@AdminNoticeBoardActivity, "Failed to add notice", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNotice(noticeId: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                firebaseHelper.deleteNotice(noticeId)
            }
            progressBar.visibility = View.GONE
            if (result) {
                Toast.makeText(this@AdminNoticeBoardActivity, "Notice deleted", Toast.LENGTH_SHORT).show()
                loadNotices()
            } else {
                Toast.makeText(this@AdminNoticeBoardActivity, "Failed to delete notice", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotices()
    }
}
