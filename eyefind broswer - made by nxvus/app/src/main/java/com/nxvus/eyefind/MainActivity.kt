package com.nxvus.eyefind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.nxvus.eyefind.model.Category
import com.nxvus.eyefind.model.SiteData
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var torManager: TorManager
    private lateinit var statusIndicator: TextView
    private lateinit var searchInput: EditText
    private lateinit var newsText: TextView
    private lateinit var websiteOfMinuteName: TextView
    private lateinit var websiteOfMinuteUrl: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        torManager = TorManager(this)
        setupViews()
        setupCategoryButtons()
        setupTorStatus()
        updateContent()
    }

    private fun setupViews() {
        statusIndicator = findViewById(R.id.statusIndicator)
        searchInput = findViewById(R.id.searchInput)
        newsText = findViewById(R.id.newsText)
        websiteOfMinuteName = findViewById(R.id.websiteOfMinuteName)
        websiteOfMinuteUrl = findViewById(R.id.websiteOfMinuteUrl)

        findViewById<Button>(R.id.searchButton).setOnClickListener {
            performSearch()
        }

        findViewById<android.view.View>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.newIdentityButton).setOnClickListener {
            torManager.requestNewIdentity()
            Toast.makeText(this, "New Tor identity requested", Toast.LENGTH_SHORT).show()
        }

        val websiteCard = findViewById<android.view.View>(R.id.websiteOfMinuteCard)
        websiteCard.setOnClickListener {
            val site = SiteData.getWebsiteOfTheMinute()
            openBrowser(site.realUrl, site.displayUrl)
        }
    }

    private fun setupCategoryButtons() {
        findViewById<MaterialButton>(R.id.btnMedia).setOnClickListener {
            openCategory(Category.MEDIA_ENTERTAINMENT)
        }
        findViewById<MaterialButton>(R.id.btnFood).setOnClickListener {
            openCategory(Category.FOOD_DRINK)
        }
        findViewById<MaterialButton>(R.id.btnMoney).setOnClickListener {
            openCategory(Category.MONEY_SERVICES)
        }
        findViewById<MaterialButton>(R.id.btnTravel).setOnClickListener {
            openCategory(Category.TRAVEL_TRANSPORT)
        }
        findViewById<MaterialButton>(R.id.btnFashion).setOnClickListener {
            openCategory(Category.FASHION_HEALTH)
        }
        findViewById<MaterialButton>(R.id.btnRandom).setOnClickListener {
            openCategory(Category.RANDOM)
        }
    }

    private fun setupTorStatus() {
        torManager.checkTorStatus()

        torManager.status.observe(this, Observer { status ->
            updateStatusIndicator(status)

            when (status) {
                TorManager.TorStatus.NOT_INSTALLED -> showOrbotInstallDialog()
                TorManager.TorStatus.INSTALLED_NOT_RUNNING -> torManager.startOrbot()
                else -> {}
            }
        })
    }

    private fun updateStatusIndicator(status: TorManager.TorStatus) {
        val (text, colorRes) = when (status) {
            TorManager.TorStatus.ACTIVE -> "Tor Active" to R.color.tor_active
            TorManager.TorStatus.STARTING -> "Tor Starting" to R.color.tor_starting
            TorManager.TorStatus.CHECKING -> "Checking Tor" to R.color.tor_starting
            TorManager.TorStatus.ERROR -> "Tor Error" to R.color.tor_inactive
            TorManager.TorStatus.NOT_INSTALLED -> "Orbot Not Installed" to R.color.tor_inactive
            TorManager.TorStatus.INSTALLED_NOT_RUNNING -> "Orbot Not Running" to R.color.tor_inactive
            TorManager.TorStatus.UNKNOWN -> "Tor Unknown" to R.color.tor_inactive
        }

        statusIndicator.text = text
        statusIndicator.setTextColor(getColor(colorRes))
    }

    private fun showOrbotInstallDialog() {
        AlertDialog.Builder(this)
            .setTitle("Orbot Required")
            .setMessage("Orbot is required for Tor functionality. Please install it from F-Droid or Google Play.")
            .setPositiveButton("Install") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/org.torproject.android/"))
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateContent() {
        newsText.text = SiteData.getRandomNews()

        val site = SiteData.getWebsiteOfTheMinute()
        websiteOfMinuteName.text = site.name
        websiteOfMinuteUrl.text = site.displayUrl
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim()

        if (query.equals("BURGER", ignoreCase = true)) {
            showLesterEasterEgg()
            return
        }

        if (query.isNotEmpty()) {
            val searchUrl = "https://duckduckgogg42xjoc72x3sjasowoarfbgcmvfimaftt6twagswzczad.onion/html/?q=${URLEncoder.encode(query, "UTF-8")}"
            openBrowser(searchUrl, "DuckDuckGo Search: $query")
        }
    }

    private fun showLesterEasterEgg() {
        AlertDialog.Builder(this)
            .setTitle("Lester's Hunting Gear")
            .setMessage("Special offer from Lester Crest! Discounted thermal imaging scopes and suppressed weapons for all your 'hunting' needs.")
            .setPositiveButton("Contact Lester") { _, _ ->
                Toast.makeText(this, "Lester has been notified. He'll call you.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Maybe Later") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openCategory(category: Category) {
        val intent = Intent(this, CategoryResultsActivity::class.java).apply {
            putExtra("category", category.name)
        }
        startActivity(intent)
    }

    private fun openBrowser(url: String, displayTitle: String) {
        val intent = Intent(this, BrowserActivity::class.java).apply {
            putExtra("url", url)
            putExtra("title", displayTitle)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        torManager.checkTorStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        torManager.unregisterReceiver()
    }
}
