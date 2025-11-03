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

class AdminComplaintsActivity : AppCompatActivity() {

    private lateinit var rvComplaints: RecyclerView
    private lateinit var firebaseHelper: FirebaseHelper
    private val complaintsList = mutableListOf<Complaint>()
    private lateinit var adapter: ComplaintAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_complaints)

        rvComplaints = findViewById(R.id.rvComplaints)
        firebaseHelper = FirebaseHelper()

        adapter = ComplaintAdapter(complaintsList) { complaint ->
            // Handle resolve button click
            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    firebaseHelper.updateComplaintStatus(complaint.complaintId, "Resolved")
                }
                if (success) {
                    Toast.makeText(this@AdminComplaintsActivity, "Complaint resolved", Toast.LENGTH_SHORT).show()
                    complaint.status = "Resolved"
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@AdminComplaintsActivity, "Failed to update complaint status", Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvComplaints.layoutManager = LinearLayoutManager(this)
        rvComplaints.adapter = adapter

        loadComplaints()
    }

    private fun loadComplaints() {
        lifecycleScope.launch {
            val complaints = withContext(Dispatchers.IO) {
                firebaseHelper.getAllComplaints()
            }
            complaintsList.clear()
            // Only show complaints which are NOT resolved
            complaintsList.addAll(complaints.filter { it.status != "Resolved" })
            adapter.notifyDataSetChanged()
        }
    }

}
