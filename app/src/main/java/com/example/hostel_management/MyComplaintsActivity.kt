package com.example.hostel_management

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyComplaintsActivity : AppCompatActivity() {

    private lateinit var rvMyComplaints: RecyclerView
    private lateinit var firebaseHelper: FirebaseHelper
    private val myComplaintsList = mutableListOf<Complaint>()
    private lateinit var adapter: ComplaintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        rvMyComplaints = findViewById(R.id.rvMyComplaints)
        firebaseHelper = FirebaseHelper()

        adapter = ComplaintAdapter(myComplaintsList, onResolveClick = { })
        rvMyComplaints.layoutManager = LinearLayoutManager(this)
        rvMyComplaints.adapter = adapter

        loadMyComplaints()
    }

    private fun loadMyComplaints() {
        val userId = firebaseHelper.getCurrentUserId()
        if (userId == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val userDetails = withContext(Dispatchers.IO) {
                firebaseHelper.getUserDetails(userId)
            }
            if (userDetails == null) {
                Toast.makeText(this@MyComplaintsActivity, "User info error", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val regNumber = userDetails.regNumber
            val allComplaints = withContext(Dispatchers.IO) {
                firebaseHelper.getAllComplaints()
            }
            // Filter only this user's complaints
            myComplaintsList.clear()
            myComplaintsList.addAll(allComplaints.filter { it.regNumber == regNumber })
            adapter.notifyDataSetChanged()
        }
    }
}
