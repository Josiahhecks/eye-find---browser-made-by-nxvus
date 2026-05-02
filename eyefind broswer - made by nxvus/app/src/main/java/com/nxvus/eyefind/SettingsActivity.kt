package com.nxvus.eyefind

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var torManager: TorManager
    private lateinit var torSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var newIdentityButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var clearOnExitSwitch: Switch
    private lateinit var progressBar: ProgressBar
    private lateinit var testConnectionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        torManager = TorManager(this)
        setupViews()
        setupTorObserver()

        findViewById<android.view.View>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupViews() {
        torSwitch = findViewById(R.id.torSwitch)
        statusText = findViewById(R.id.statusText)
        newIdentityButton = findViewById(R.id.newIdentityButton)
        clearDataButton = findViewById(R.id.clearDataButton)
        clearOnExitSwitch = findViewById(R.id.clearOnExitSwitch)
        progressBar = findViewById(R.id.progressBar)
        testConnectionButton = findViewById(R.id.testConnectionButton)

        val prefs = getSharedPreferences("eyefind_prefs", Context.MODE_PRIVATE)
        torSwitch.isChecked = prefs.getBoolean("tor_enabled", true)
        clearOnExitSwitch.isChecked = prefs.getBoolean("clear_on_exit", false)

        torSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                showTorDisableWarning()
            } else {
                prefs.edit().putBoolean("tor_enabled", true).apply()
                torManager.startOrbot()
            }
        }

        newIdentityButton.setOnClickListener {
            torManager.requestNewIdentity()
            Toast.makeText(this, "New Tor identity requested", Toast.LENGTH_SHORT).show()
        }

        clearDataButton.setOnClickListener {
            clearBrowsingData(showToast = true)
        }

        clearOnExitSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("clear_on_exit", isChecked).apply()
        }

        testConnectionButton.setOnClickListener {
            testTorConnection()
        }
    }

    private fun setupTorObserver() {
        torManager.status.observe(this, Observer { status ->
            statusText.text = "Status: ${status.name}"

            when (status) {
                TorManager.TorStatus.ACTIVE -> {
                    torSwitch.isChecked = true
                    newIdentityButton.isEnabled = true
                }
                TorManager.TorStatus.NOT_INSTALLED -> {
                    torSwitch.isEnabled = false
                    newIdentityButton.isEnabled = false
                    statusText.text = "Status: Orbot not installed"
                }
                else -> {
                    newIdentityButton.isEnabled = status == TorManager.TorStatus.ACTIVE
                }
            }
        })

        torManager.checkTorStatus()
    }

    private fun showTorDisableWarning() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Warning")
            .setMessage("Disabling Tor will expose your real IP address and browsing activity. This is NOT recommended.")
            .setPositiveButton("Disable Anyway") { _, _ ->
                getSharedPreferences("eyefind_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("tor_enabled", false).apply()
                torManager.stopOrbot()
            }
            .setNegativeButton("Keep Tor On") { _, _ ->
                torSwitch.isChecked = true
            }
            .setCancelable(false)
            .show()
    }

    private fun clearBrowsingData(showToast: Boolean) {
        deleteDatabase("webview.db")
        deleteDatabase("webviewCache.db")

        cacheDir.deleteRecursively()

        if (showToast) {
            Toast.makeText(this, "Browsing data cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testTorConnection() {
        progressBar.visibility = android.view.View.VISIBLE
        testConnectionButton.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val isTorWorking = torManager.testTorConnection()

            progressBar.visibility = android.view.View.GONE
            testConnectionButton.isEnabled = true

            if (isTorWorking) {
                Toast.makeText(this@SettingsActivity, "Tor is working correctly", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@SettingsActivity, "Tor connection failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        if (getSharedPreferences("eyefind_prefs", Context.MODE_PRIVATE).getBoolean("clear_on_exit", false)) {
            clearBrowsingData(showToast = false)
        }
        super.onDestroy()
        torManager.unregisterReceiver()
    }
}
