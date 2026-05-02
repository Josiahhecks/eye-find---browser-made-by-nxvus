package com.nxvus.eyefind.model

import com.nxvus.eyefind.R

enum class Category {
    MEDIA_ENTERTAINMENT,
    FOOD_DRINK,
    MONEY_SERVICES,
    TRAVEL_TRANSPORT,
    FASHION_HEALTH,
    RANDOM
}

data class EyefindSite(
    val name: String,
    val realUrl: String,
    val displayUrl: String,
    val description: String,
    val category: Category,
    val iconRes: Int = 0
)

object SiteData {
    private fun iconFor(category: Category): Int = when (category) {
        Category.MEDIA_ENTERTAINMENT -> R.drawable.ic_media
        Category.FOOD_DRINK -> R.drawable.ic_food
        Category.MONEY_SERVICES -> R.drawable.ic_money
        Category.TRAVEL_TRANSPORT -> R.drawable.ic_travel
        Category.FASHION_HEALTH -> R.drawable.ic_fashion
        Category.RANDOM -> R.drawable.ic_random
    }

    private fun site(
        name: String,
        realUrl: String,
        displayUrl: String,
        description: String,
        category: Category
    ) = EyefindSite(name, realUrl, displayUrl, description, category, iconFor(category))

    fun getSitesByCategory(category: Category): List<EyefindSite> {
        return when (category) {
            Category.MEDIA_ENTERTAINMENT -> listOf(
                site(
                    "Lifeinvader",
                    "https://gta.fandom.com/wiki/Lifeinvader",
                    "www.lifeinvader.com",
                    "The social networking site where stalking is caring. Share your every thought with people you barely know.",
                    category
                ),
                site(
                    "Classic Vinewood",
                    "https://gta.fandom.com/wiki/Classic_Vinewood",
                    "www.classicvinewood.com",
                    "Celebrating the golden age of Vinewood cinema. Relive the movies that defined a generation of excess.",
                    category
                ),
                site(
                    "Fame or Shame",
                    "https://gta.fandom.com/wiki/Fame_or_Shame",
                    "www.fameorshame.net",
                    "The talent show where dreams are crushed and dignity is optional. Hosted by Lazlow Jones.",
                    category
                ),
                site(
                    "Kung Fu Rainbow Lazer Force",
                    "https://gta.fandom.com/wiki/Kung_Fu_Rainbow_Lazer_Force",
                    "www.kungfurainbowlazerforce.tv",
                    "Christian values meets martial arts action. Teaching kids important lessons through violence.",
                    category
                ),
                site(
                    "Princess Robot Bubblegum",
                    "https://gta.fandom.com/wiki/Princess_Robot_Bubblegum",
                    "www.princessrobotbubblegum.jp",
                    "The hit anime about a princess who fights crime while dealing with existential angst.",
                    category
                ),
                site(
                    "Republican Space Rangers",
                    "https://gta.fandom.com/wiki/Republican_Space_Rangers",
                    "www.republicanspacerangers.com",
                    " defending democracy across the galaxy, one questionable decision at a time.",
                    category
                ),
                site(
                    "Righteous Slaughter 7",
                    "https://gta.fandom.com/wiki/Righteous_Slaughter_7",
                    "www.righteousslaughter7.com",
                    "The most realistic military shooter ever made. Now with 40% more righteous indignation.",
                    category
                )
            )
            Category.FOOD_DRINK -> listOf(
                site(
                    "Burger Shot",
                    "https://gta.fandom.com/wiki/Burger_Shot",
                    "www.burgershot.net",
                    "Home of the Bleeder. Taste the difference quality makes (results may vary).",
                    category
                ),
                site(
                    "Cluckin' Bell",
                    "https://gta.fandom.com/wiki/Cluckin%27_Bell",
                    "www.cluckinbell.com",
                    "Cock-a-doodle-doo, it's time for chicken! Now with 11 herbs and sedatives.",
                    category
                ),
                site(
                    "Bean Machine",
                    "https://gta.fandom.com/wiki/Bean_Machine",
                    "www.beanmachinecoffee.com",
                    "The finest overpriced coffee experience. Taste the exploitation.",
                    category
                ),
                site(
                    "Logger Beer",
                    "https://gta.fandom.com/wiki/Logger_Beer",
                    "www.loggerbeer.com",
                    "The beer that made Los Santos great. Or at least more tolerable.",
                    category
                ),
                site(
                    "Pißwasser",
                    "https://gta.fandom.com/wiki/Pi%C3%9Fwasser",
                    "www.pisswasser.com",
                    "German fighting lager. You're guaranteed to love it after 12 bottles.",
                    category
                ),
                site(
                    "Taco Bomb",
                    "https://gta.fandom.com/wiki/Taco_Bomb",
                    "www.tacobomb.com",
                    "Authentic Mexican cuisine, American style. Explosive flavor guaranteed.",
                    category
                )
            )
            Category.MONEY_SERVICES -> listOf(
                site(
                    "Maze Bank",
                    "https://gta.fandom.com/wiki/Maze_Bank",
                    "www.maze-bank.com",
                    "Your money is safe with us. Probably. Terms and conditions apply.",
                    category
                ),
                site(
                    "Bawsaq",
                    "https://gta.fandom.com/wiki/BAWSAQ",
                    "www.bawsaq.com",
                    "The stock market for the rest of us. Invest in your financial future today.",
                    category
                ),
                site(
                    "Legendary Motorsport",
                    "https://gta.fandom.com/wiki/Legendary_Motorsport",
                    "www.legendarymotorsport.net",
                    "Premium vehicles for premium people. Financing available for qualified buyers.",
                    category
                ),
                site(
                    "Dynasty 8",
                    "https://gta.fandom.com/wiki/Dynasty_8",
                    "www.dynasty8realestate.com",
                    "Luxury real estate in Los Santos. Because you're worth the inflated prices.",
                    category
                ),
                site(
                    "Elitas Travel",
                    "https://gta.fandom.com/wiki/Elitas_Travel",
                    "www.elitastravel.com",
                    "First-class travel for the discerning criminal. I mean, citizen.",
                    category
                ),
                site(
                    "Lombank",
                    "https://gta.fandom.com/wiki/Lombank",
                    "www.lombank.com",
                    "Banking with a personal touch. We've been losing money since 1954.",
                    category
                )
            )
            Category.TRAVEL_TRANSPORT -> listOf(
                site(
                    "Los Santos International",
                    "https://gta.fandom.com/wiki/Los_Santos_International_Airport",
                    "www.losantosairport.com",
                    "Your gateway to the world. Security Theater provided free of charge.",
                    category
                ),
                site(
                    "Downtown Cab Co.",
                    "https://gta.fandom.com/wiki/Downtown_Cab_Co.",
                    "www.downtowncabs.com",
                    "We'll get you there eventually. Complimentary air freshener included.",
                    category
                ),
                site(
                    "Warstock Cache & Carry",
                    "https://gta.fandom.com/wiki/Warstock_Cache_%26_Carry",
                    "www.warstock-cache-and-carry.com",
                    "Military-grade vehicles for civilian use. What could go wrong?",
                    category
                ),
                site(
                    "DockTease",
                    "https://gta.fandom.com/wiki/DockTease",
                    "www.docktease.com",
                    "Luxury marine vessels. Because the ocean is just another highway.",
                    category
                ),
                site(
                    "Pedal and Metal",
                    "https://gta.fandom.com/wiki/Pedal_and_Metal_Cycles",
                    "www.pedalandmetal.com",
                    "Bicycles for the eco-conscious criminal. Zero emissions, maximum cardio.",
                    category
                ),
                site(
                    "Ammu-Nation",
                    "https://gta.fandom.com/wiki/Ammu-Nation",
                    "www.ammunation.net",
                    "Protecting your rights, one firearm at a time. Constitutional carry encouraged.",
                    category
                )
            )
            Category.FASHION_HEALTH -> listOf(
                site(
                    "Binco",
                    "https://gta.fandom.com/wiki/Binco",
                    "www.binco.com",
                    "Fashion for the masses. None of that pretentious designer nonsense.",
                    category
                ),
                site(
                    "Ponsonbys",
                    "https://gta.fandom.com/wiki/Ponsonbys",
                    "www.ponsonbys.com",
                    "Luxury fashion for those who've made questionable life choices successfully.",
                    category
                ),
                site(
                    "Discount Hospital",
                    "https://gta.fandom.com/wiki/Hospitals",
                    "www.discounthospital.com",
                    "Medical care you can almost afford. Results not guaranteed.",
                    category
                ),
                site(
                    "Vanilla Unicorn",
                    "https://gta.fandom.com/wiki/Vanilla_Unicorn",
                    "www.vanillaunicorn.com",
                    "Los Santos' premier gentleman's club. Classy entertainment for discerning patrons.",
                    category
                ),
                site(
                    "Didier Sachs",
                    "https://gta.fandom.com/wiki/Didier_Sachs",
                    "www.didiersachs.com",
                    "Timeless elegance for the 1%. You've arrived when you shop here.",
                    category
                ),
                site(
                    "Bitch'n' Dog Food",
                    "https://gta.fandom.com/wiki/Bitch%27n%27_Dog_Food",
                    "www.bitchnfood.com",
                    "Premium nutrition for your four-legged companion. They deserve better than you.",
                    category
                )
            )
            Category.RANDOM -> getAllSites().shuffled().take(6)
        }
    }

    fun getAllSites(): List<EyefindSite> {
        return Category.values().filter { it != Category.RANDOM }
            .flatMap { getSitesByCategory(it) }
    }

    fun getRandomNews(): String {
        val news = listOf(
            "Rash of ATM robberies across Los Santos - police baffled by criminals using explosives",
            "Epsilon Program promises enlightenment for just $5,000 donation - limited time offer",
            "Vinewood producer accused of inappropriate casting practices - claims artistic freedom",
            "Los Santos Dodos football team loses again - fans demand miracle",
            "Traffic congestion at record highs - experts suggest buying a helicopter",
            "Merryweather Security denies civilian casualties - releases statement via tank",
            "Weazel News investigation finds water is wet - more at 11",
            "Fame or Shame contestant hospitalized after failed stunt - ratings soar",
            "Tech billionaire announces Mars colony - volunteers must waive all rights",
            "Vinewood dog restaurant controversy - just a misunderstanding claims owner"
        )
        return news.random()
    }

    fun getWebsiteOfTheMinute(): EyefindSite {
        return getAllSites().random()
    }
}
