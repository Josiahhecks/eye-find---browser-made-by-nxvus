package com.nxvus.eyefind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nxvus.eyefind.model.Category
import com.nxvus.eyefind.model.EyefindSite
import com.nxvus.eyefind.model.SiteData

class CategoryResultsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryTitle: TextView
    private lateinit var backButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_results)

        val categoryName = intent.getStringExtra("category") ?: Category.MEDIA_ENTERTAINMENT.name
        val category = Category.valueOf(categoryName)

        setupViews(category)
        loadSites(category)
    }

    private fun setupViews(category: Category) {
        categoryTitle = findViewById(R.id.categoryTitle)
        recyclerView = findViewById(R.id.recyclerView)
        backButton = findViewById(R.id.backButton)

        categoryTitle.text = when (category) {
            Category.MEDIA_ENTERTAINMENT -> "MEDIA AND ENTERTAINMENT"
            Category.FOOD_DRINK -> "FOOD AND DRINK"
            Category.MONEY_SERVICES -> "MONEY AND SERVICES"
            Category.TRAVEL_TRANSPORT -> "TRAVEL AND TRANSPORT"
            Category.FASHION_HEALTH -> "FASHION AND HEALTH"
            Category.RANDOM -> "RANDOM"
        }

        backButton.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.homeButton).setOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadSites(category: Category) {
        val sites = SiteData.getSitesByCategory(category)
        recyclerView.adapter = SiteAdapter(sites) { site ->
            openBrowser(site)
        }
    }

    private fun openBrowser(site: EyefindSite) {
        val intent = Intent(this, BrowserActivity::class.java).apply {
            putExtra("url", site.realUrl)
            putExtra("title", site.name)
            putExtra("displayUrl", site.displayUrl)
        }
        startActivity(intent)
    }

    class SiteAdapter(
        private val sites: List<EyefindSite>,
        private val onClick: (EyefindSite) -> Unit
    ) : RecyclerView.Adapter<SiteAdapter.SiteViewHolder>() {

        class SiteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.siteName)
            val urlText: TextView = view.findViewById(R.id.siteUrl)
            val descText: TextView = view.findViewById(R.id.siteDescription)
            val icon: ImageView = view.findViewById(R.id.siteIcon)
            val card: View = view.findViewById(R.id.siteCard)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_site, parent, false)
            return SiteViewHolder(view)
        }

        override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
            val site = sites[position]
            holder.nameText.text = site.name
            holder.urlText.text = site.displayUrl
            holder.descText.text = site.description
            holder.icon.setImageResource(site.iconRes)

            holder.card.setOnClickListener {
                onClick(site)
            }
        }

        override fun getItemCount() = sites.size
    }
}
