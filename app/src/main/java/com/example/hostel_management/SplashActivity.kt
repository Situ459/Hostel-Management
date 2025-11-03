package com.example.hostel_management

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Show splash for 2 seconds then go to Login
        Handler(Looper.getMainLooper()).postDelayed({
            val firebaseHelper = FirebaseHelper()

            val intent = if (firebaseHelper.isUserLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2000) // 2 seconds
    }
}
