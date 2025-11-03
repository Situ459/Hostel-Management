package com.example.hostel_management

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button
    private lateinit var cardNoticeBoard: CardView
    private lateinit var cardUpcomingEvents: CardView
    private lateinit var cardComplaints: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Setup toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        cardNoticeBoard = findViewById(R.id.cardNoticeBoard)
        cardUpcomingEvents = findViewById(R.id.cardUpcomingEvents)
        cardComplaints = findViewById(R.id.cardComplaints)

        // Set welcome message with admin's name from login/session
        val adminName = "Admin Name" // Replace with actual admin name logic
        tvWelcome.text = "Welcome, $adminName"

        cardNoticeBoard.setOnClickListener {
            startActivity(Intent(this, AdminNoticeBoardActivity::class.java))
        }

        cardUpcomingEvents.setOnClickListener {
            startActivity(Intent(this, AdminUpcomingEventsActivity::class.java))
        }

        cardComplaints.setOnClickListener {
            startActivity(Intent(this, AdminComplaintsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            FirebaseHelper().logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Inflate the admin toolbar menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_toolbar_menu, menu)
        return true
    }

    // Handle menu item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, AdminProfileActivity::class.java))
                return true
            }
            R.id.action_settings -> {
                // Add intent or action for settings
                return true
            }
            R.id.action_logout -> {
                FirebaseHelper().logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
