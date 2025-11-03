package com.example.hostel_management

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        // Example: Getting admin info from Intent or shared preferences
        val adminName = "Your Admin Name" // Get from your session/auth system
        val adminId = "ADMIN001"          // Get from your database/session

        findViewById<TextView>(R.id.tvAdminName).text = adminName
        findViewById<TextView>(R.id.tvAdminId).text = adminId
    }
}
