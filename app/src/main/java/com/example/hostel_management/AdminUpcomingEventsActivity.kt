package com.example.hostel_management

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminUpcomingEventsActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_upcoming_events)

        tvTitle = findViewById(R.id.tvAdminUpcomingEventsTitle)

        // Set title or any other initial UI setup
        tvTitle.text = "Admin Upcoming Events"

        // TODO: Add your admin event management logic here
        // For example, fetch upcoming events from database and display in a list
    }
}
