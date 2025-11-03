package com.example.hostel_management

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tvUserNameToolbar: TextView
    private lateinit var tvRegNumber: TextView
    private lateinit var tvRoomNumber: TextView
    private lateinit var floatingOverflowMenu: ImageView
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseHelper = FirebaseHelper()

        if (!firebaseHelper.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        initViews()
        setupToolbar()
        setupNavigationDrawer()
        setupCardClicks()
        setupFloatingOverflowMenu()
        loadUserData()
        setupBackPress()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        tvUserNameToolbar = findViewById(R.id.tvUserNameToolbar)
        tvRegNumber = findViewById(R.id.tvRegNumber)
        tvRoomNumber = findViewById(R.id.tvRoomNumber)
        floatingOverflowMenu = findViewById(R.id.floatingOverflowMenu)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        val headerView = navigationView.getHeaderView(0)
        val navHeaderName = headerView.findViewById<TextView>(R.id.navHeaderName)
        val btnViewProfile = headerView.findViewById<Button>(R.id.btnViewProfile)

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val userId = firebaseHelper.getCurrentUserId()
                val userDetails = userId?.let { withContext(Dispatchers.IO) { firebaseHelper.getUserDetails(it) } }
                if (userDetails != null) {
                    navHeaderName.text = userDetails.name
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnViewProfile.setOnClickListener {
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCardClicks() {
        findViewById<CardView>(R.id.cardNoticeBoard).setOnClickListener {
            // Launch student NoticeBoardActivity
            val intent = Intent(this, NoticeBoardActivity::class.java)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardUpcomingEvents).setOnClickListener {
            Toast.makeText(this, "Upcoming Events feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardNewsUpdates).setOnClickListener {
            Toast.makeText(this, "News & Updates feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.cardSubmitComplaint).setOnClickListener {
            val intent = Intent(this, SubmitComplaintActivity::class.java)
            startActivity(intent)
        }

        // UPDATED: My Complaints card click opens MyComplaintsActivity
        findViewById<CardView>(R.id.cardMyComplaints).setOnClickListener {
            val intent = Intent(this, MyComplaintsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupFloatingOverflowMenu() {
        floatingOverflowMenu.setOnClickListener {
            // inflating custom dialog menu layout
            val dialogView = layoutInflater.inflate(R.layout.dialog_menu, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<LinearLayout>(R.id.itemRoomDetails).setOnClickListener {
                val intent = Intent(this, RoomDetailActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }
            dialogView.findViewById<LinearLayout>(R.id.itemFees).setOnClickListener {
                Toast.makeText(this, "Fees coming soon!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialogView.findViewById<LinearLayout>(R.id.itemMessMenu).setOnClickListener {
                val intent = Intent(this, MessMenuActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }
            dialogView.findViewById<LinearLayout>(R.id.itemComplain).setOnClickListener {
                val intent = Intent(this, SubmitComplaintActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun loadUserData() {
        val userId = firebaseHelper.getCurrentUserId()

        if (userId == null) {
            navigateToLogin()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val userDetails = withContext(Dispatchers.IO) {
                    firebaseHelper.getUserDetails(userId)
                }

                if (userDetails != null) {
                    tvUserNameToolbar.text = "Welcome, ${userDetails.name}"
                    tvRegNumber.text = userDetails.regNumber
                    tvRoomNumber.text = userDetails.roomNumber
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish()
            }
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_help -> {
                Toast.makeText(this, "Help & Support coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "About coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                firebaseHelper.logout()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
