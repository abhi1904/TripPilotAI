package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.ActivityTemplate
import com.trippilot.trippilotai.domain.model.Destination
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.Interest.ADVENTURE
import com.trippilot.trippilotai.domain.model.Interest.CULTURE
import com.trippilot.trippilotai.domain.model.Interest.FOOD
import com.trippilot.trippilotai.domain.model.Interest.NATURE
import com.trippilot.trippilotai.domain.model.Interest.RELAXATION
import com.trippilot.trippilotai.domain.model.Interest.SHOPPING
import com.trippilot.trippilotai.domain.model.Scene

/** Local, hard-coded destinations. Prices are illustrative INR estimates only. */
object MockDestinations {

    val all: List<Destination> = listOf(
        Destination(
            id = "goa", name = "Goa", region = "India", tagline = "Sun, sand and spice",
            scene = Scene.BEACH, rating = 4.7,
            stayPerNight = 4500, foodPerPersonPerDay = 1200, localTransportPerDay = 900, returnTravelPerPerson = 6500,
            activities = listOf(
                ActivityTemplate(RELAXATION, "Palolem Beach morning", "Slow swim and coconut water on a quiet crescent beach.", 0),
                ActivityTemplate(CULTURE, "Old Goa churches walk", "Basilica of Bom Jesus and Se Cathedral with a local guide.", 600),
                ActivityTemplate(FOOD, "Goan seafood thali", "Fish curry rice and kokum at a family-run shack.", 700),
                ActivityTemplate(ADVENTURE, "Grande Island snorkelling", "Boat trip with snorkelling stops and dolphin spotting.", 2200),
                ActivityTemplate(NATURE, "Dudhsagar Falls trip", "Jeep ride through the Western Ghats to the four-tier falls.", 1800),
                ActivityTemplate(SHOPPING, "Anjuna flea market", "Handicrafts, spices and beachwear at the Wednesday market.", 500),
                ActivityTemplate(CULTURE, "Fontainhas Latin Quarter", "Colourful Portuguese-era lanes and art galleries.", 0),
                ActivityTemplate(FOOD, "Spice plantation lunch", "Guided plantation tour with a traditional buffet.", 900),
                ActivityTemplate(RELAXATION, "Sunset at Chapora Fort", "Golden-hour views over Vagator beach.", 0),
            ),
        ),
        Destination(
            id = "manali", name = "Manali", region = "Himachal Pradesh", tagline = "Pine forests and snow peaks",
            scene = Scene.MOUNTAIN, rating = 4.6,
            stayPerNight = 3800, foodPerPersonPerDay = 1000, localTransportPerDay = 1500, returnTravelPerPerson = 5500,
            activities = listOf(
                ActivityTemplate(NATURE, "Solang Valley views", "Cable car ride with wide Himalayan panoramas.", 1200),
                ActivityTemplate(ADVENTURE, "Paragliding at Solang", "Tandem flight with a certified pilot.", 3000),
                ActivityTemplate(CULTURE, "Hadimba Temple", "Wooden pagoda temple set in a cedar forest.", 0),
                ActivityTemplate(FOOD, "Old Manali café crawl", "Trout, siddu and apple pie at riverside cafés.", 800),
                ActivityTemplate(RELAXATION, "Vashisht hot springs", "Natural sulphur springs and a quiet village stroll.", 100),
                ActivityTemplate(SHOPPING, "Mall Road & Tibetan market", "Shawls, woollens and handmade souvenirs.", 600),
                ActivityTemplate(ADVENTURE, "Beas river rafting", "Grade II–III rapids near Kullu.", 1500),
                ActivityTemplate(NATURE, "Jogini Falls hike", "Easy forest trail ending at a waterfall.", 0),
            ),
        ),
        Destination(
            id = "jaipur", name = "Jaipur", region = "Rajasthan", tagline = "Palaces of the Pink City",
            scene = Scene.DESERT, rating = 4.6,
            stayPerNight = 4000, foodPerPersonPerDay = 1100, localTransportPerDay = 1000, returnTravelPerPerson = 5000,
            activities = listOf(
                ActivityTemplate(CULTURE, "Amber Fort", "Hilltop fort with mirror palace and courtyard views.", 700),
                ActivityTemplate(CULTURE, "City Palace & Jantar Mantar", "Royal residence and UNESCO-listed observatory.", 900),
                ActivityTemplate(FOOD, "Rajasthani thali dinner", "Dal baati churma and gatte ki sabzi.", 900),
                ActivityTemplate(SHOPPING, "Johari & Bapu Bazaar", "Block prints, jewellery and mojari shoes.", 0),
                ActivityTemplate(ADVENTURE, "Hot-air balloon ride", "Sunrise flight over forts and villages.", 9500),
                ActivityTemplate(NATURE, "Nahargarh sunset", "Fort ramparts overlooking the whole city.", 200),
                ActivityTemplate(RELAXATION, "Haveli spa afternoon", "Ayurvedic massage in a heritage haveli.", 2500),
                ActivityTemplate(FOOD, "Old city street food", "Pyaaz kachori, lassi and ghewar tasting walk.", 500),
            ),
        ),
        Destination(
            id = "kerala", name = "Alleppey", region = "Kerala", tagline = "Houseboats on quiet backwaters",
            scene = Scene.BACKWATERS, rating = 4.8,
            stayPerNight = 5000, foodPerPersonPerDay = 1100, localTransportPerDay = 1200, returnTravelPerPerson = 7000,
            activities = listOf(
                ActivityTemplate(RELAXATION, "Houseboat cruise", "Drift past paddy fields and palm-lined canals.", 2500),
                ActivityTemplate(NATURE, "Village canoe ride", "Narrow canals and birdlife by country boat.", 800),
                ActivityTemplate(FOOD, "Karimeen & appam lunch", "Pearl-spot fish in banana leaf with appam.", 600),
                ActivityTemplate(CULTURE, "Kathakali performance", "Classical dance-drama with make-up demonstration.", 500),
                ActivityTemplate(RELAXATION, "Ayurvedic massage", "Traditional abhyanga oil therapy.", 2000),
                ActivityTemplate(ADVENTURE, "Kayaking at dawn", "Guided paddle through misty backwaters.", 1200),
                ActivityTemplate(SHOPPING, "Coir & spice shopping", "Local coir crafts and fresh spices.", 400),
                ActivityTemplate(NATURE, "Marari beach evening", "Quiet fishing-village beach at sunset.", 0),
            ),
        ),
        Destination(
            id = "bali", name = "Bali", region = "Indonesia", tagline = "Temples, rice terraces and surf",
            scene = Scene.ISLAND, rating = 4.8,
            stayPerNight = 5500, foodPerPersonPerDay = 1800, localTransportPerDay = 1400, returnTravelPerPerson = 28000,
            activities = listOf(
                ActivityTemplate(NATURE, "Tegallalang rice terraces", "Morning walk through emerald terraces.", 400),
                ActivityTemplate(CULTURE, "Uluwatu temple & Kecak", "Cliff-top temple and fire-dance at sunset.", 1200),
                ActivityTemplate(ADVENTURE, "Mount Batur sunrise trek", "Pre-dawn volcano hike with breakfast.", 3500),
                ActivityTemplate(FOOD, "Balinese cooking class", "Market visit and hands-on cooking.", 2500),
                ActivityTemplate(RELAXATION, "Ubud spa & yoga", "Balinese massage and an open-air yoga class.", 2200),
                ActivityTemplate(SHOPPING, "Ubud art market", "Batik, silver and woven bags.", 0),
                ActivityTemplate(ADVENTURE, "Surf lesson in Canggu", "Beginner lesson with board hire.", 2000),
                ActivityTemplate(NATURE, "Tirta Gangga water palace", "Royal gardens and stepping-stone ponds.", 300),
            ),
        ),
        Destination(
            id = "kyoto", name = "Kyoto", region = "Japan", tagline = "Shrines, gardens and tea",
            scene = Scene.TEMPLE_CITY, rating = 4.9,
            stayPerNight = 9000, foodPerPersonPerDay = 3000, localTransportPerDay = 1200, returnTravelPerPerson = 55000,
            activities = listOf(
                ActivityTemplate(CULTURE, "Fushimi Inari gates", "Early walk through thousands of torii gates.", 0),
                ActivityTemplate(NATURE, "Arashiyama bamboo grove", "Bamboo forest and riverside park.", 0),
                ActivityTemplate(CULTURE, "Tea ceremony", "Traditional matcha ceremony in a machiya house.", 2800),
                ActivityTemplate(FOOD, "Nishiki Market tasting", "Pickles, yuba and street snacks.", 1500),
                ActivityTemplate(RELAXATION, "Onsen evening", "Relaxing hot-spring bath in Kurama.", 1600),
                ActivityTemplate(SHOPPING, "Teramachi arcade", "Ceramics, stationery and souvenirs.", 0),
                ActivityTemplate(ADVENTURE, "Hozugawa river boat", "Rapids ride through a forested gorge.", 2600),
                ActivityTemplate(CULTURE, "Gion evening stroll", "Lantern-lit lanes of the geisha district.", 0),
            ),
        ),
    )

    fun byName(name: String): Destination? =
        all.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) || it.id.equals(name.trim(), ignoreCase = true) }

    /** Returns a known destination, or a generic placeholder for free-text input. */
    fun findOrGeneric(name: String): Destination = byName(name) ?: generic(name.trim())

    private fun generic(name: String) = Destination(
        id = "custom-${name.lowercase().replace(Regex("[^a-z0-9]+"), "-")}",
        name = name.replaceFirstChar { it.uppercase() },
        region = "Custom destination",
        tagline = "Your own adventure",
        scene = Scene.MOUNTAIN,
        rating = 4.5,
        stayPerNight = 4500, foodPerPersonPerDay = 1300, localTransportPerDay = 1200, returnTravelPerPerson = 8000,
        activities = Interest.entries.map { interest ->
            ActivityTemplate(
                interest = interest,
                title = "${interest.label} highlights of ${name.replaceFirstChar { it.uppercase() }}",
                description = "A popular ${interest.label.lowercase()} experience suggested for this area.",
                costPerPerson = if (interest == Interest.RELAXATION) 0 else 800,
            )
        },
    )
}
