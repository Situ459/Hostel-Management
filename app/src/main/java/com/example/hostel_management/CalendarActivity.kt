package com.example.hostel_management

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var tvEventDetails: TextView

    private val events = mapOf(
        "2025-12-25" to "Christmas Festival"
        // add more yyyy-MM-dd to event text
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        tvEventDetails = findViewById(R.id.tvEventDetails)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            val event = events[selectedDate]
            if (event != null) {
                tvEventDetails.text = "Events on $selectedDate:\n$event"
            } else {
                tvEventDetails.text = "No events on $selectedDate"
            }
        }
    }
}

