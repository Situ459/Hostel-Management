package com.example.hostel_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplaintAdapter(
    private val complaints: List<Complaint>,
    private val onResolveClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {

    inner class ComplaintViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvCategories: TextView = view.findViewById(R.id.tvCategories)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnResolve: Button = view.findViewById(R.id.btnResolve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_complaint, parent, false)
        return ComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaints[position]
        holder.tvName.text = "${complaint.name} (${complaint.regNumber}), Room: ${complaint.roomNumber}"
        holder.tvCategories.text = "Category: ${complaint.categories.joinToString()}"
        holder.tvText.text = complaint.complaintText
        holder.tvStatus.text = "Status: ${complaint.status}"
        holder.btnResolve.isEnabled = complaint.status != "Resolved"
        holder.btnResolve.setOnClickListener {
            onResolveClick(complaint)
        }
    }

    override fun getItemCount(): Int = complaints.size
}
