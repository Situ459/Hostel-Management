package com.example.hostel_management

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class FeesActivity : AppCompatActivity() {

    private lateinit var tblFees: TableLayout
    private lateinit var btnPayNow: Button
    private var totalFee: Int = 0
    private val session = "2025fall"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fees)

        tblFees = findViewById(R.id.tblFees)
        btnPayNow = findViewById(R.id.btnPayNow)

        fetchAndDisplayFeeForUser()

        btnPayNow.setOnClickListener {
            launchUpiIntent(totalFee)
        }
    }

    private fun fetchAndDisplayFeeForUser() {
        tblFees.removeAllViews()
        val headerRow = TableRow(this)
        headerRow.addView(makeCell("Item", true))
        headerRow.addView(makeCell("Amount", true))
        tblFees.addView(headerRow)

        val firebaseHelper = FirebaseHelper()
        val userId = firebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val feesMap = firebaseHelper.getFees(session)
            val userPaid = firebaseHelper.getUserFeeStatus(userId, session)
            totalFee = if (userPaid) 0 else (feesMap["total"] ?: 0)
            for ((item, amount) in feesMap) {
                if (item != "total") {
                    tblFees.addView(makeRow(item.replaceFirstChar(Char::uppercase), "₹$amount"))
                }
            }
            tblFees.addView(makeRow("Total", "₹$totalFee", true))
            btnPayNow.isEnabled = totalFee > 0
        }
    }

    private fun makeRow(key: String, value: String, bold: Boolean = false): TableRow {
        val row = TableRow(this)
        row.addView(makeCell(key, bold))
        row.addView(makeCell(value, bold))
        return row
    }

    private fun makeCell(text: String, bold: Boolean = false): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 16f
        tv.setPadding(10, 10, 10, 10)
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD)
        return tv
    }

    private fun launchUpiIntent(amount: Int) {
        if (amount <= 0) {
            Toast.makeText(this, "No payment required", Toast.LENGTH_SHORT).show()
            return
        }
        val upiUri = Uri.parse(
            "upi://pay?pa=9348001384@fam&pn=Hostel_Fees&tn=Hostel+Fee+Payment&am=$amount&cu=INR"
        )
        val intent = Intent(Intent.ACTION_VIEW, upiUri)
        val chooser = Intent.createChooser(intent, "Pay with")
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, 11)
        } else {
            Toast.makeText(this, "No UPI app found!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val firebaseHelper = FirebaseHelper()
        val userId = firebaseHelper.getCurrentUserId() ?: return

        if (requestCode == 11 && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                firebaseHelper.markUserFeePaid(userId, session)
                fetchAndDisplayFeeForUser()
                Toast.makeText(this@FeesActivity, "Payment marked as successful", Toast.LENGTH_LONG).show()
            }
        }
    }
}
