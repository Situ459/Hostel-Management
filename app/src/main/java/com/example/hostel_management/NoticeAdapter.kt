package com.example.hostel_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoticeAdapter(
    private val notices: List<Notice>,
    private val onDelete: (Notice) -> Unit,
    private var isAdmin: Boolean // Pass true in AdminNoticeBoard, false in student
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    fun setIsAdmin(admin: Boolean) {
        this.isAdmin = admin
        notifyDataSetChanged()
    }

    inner class NoticeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNoticeTitle)
        val tvContent: TextView = view.findViewById(R.id.tvNoticeContent)
        val tvTimestamp: TextView = view.findViewById(R.id.tvNoticeTime)
        val btnDelete: ImageButton? = view.findViewById(R.id.btnDeleteNotice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]
        holder.tvTitle.text = notice.title
        holder.tvContent.text = notice.content

        holder.tvTimestamp.text = android.text.format.DateFormat.format(
            "dd MMM yyyy, hh:mm a", notice.timestamp
        )

        if (holder.btnDelete != null) {
            holder.btnDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
            holder.btnDelete.setOnClickListener { onDelete(notice) }
        }
    }

    override fun getItemCount(): Int = notices.size
}
