package com.trippilot.trippilotai.domain.model

import kotlinx.serialization.Serializable

/** Visual style used to draw a destination's scenic artwork. */
enum class Scene { BEACH, MOUNTAIN, DESERT, BACKWATERS, ISLAND, TEMPLE_CITY }

enum class Interest(val label: String) {
    NATURE("Nature"),
    CULTURE("Culture"),
    FOOD("Food"),
    ADVENTURE("Adventure"),
    RELAXATION("Relaxation"),
    SHOPPING("Shopping"),
}

/** A reusable activity idea for a destination, tagged with the interest it serves. */
@Serializable
data class ActivityTemplate(
    val interest: Interest,
    val title: String,
    val description: String,
    val costPerPerson: Int,
)

/** A place offered while the user types a destination. */
data class DestinationSuggestion(val name: String, val region: String)

/** Mock destination. All amounts are illustrative INR estimates, not live prices. */
@Serializable
data class Destination(
    val id: String,
    val name: String,
    val region: String,
    val tagline: String,
    val scene: Scene,
    val rating: Double,
    val stayPerNight: Int,
    val foodPerPersonPerDay: Int,
    val localTransportPerDay: Int,
    val returnTravelPerPerson: Int,
    val activities: List<ActivityTemplate>,
) {
    /** Rough "from" price per person for a 3-night trip, used on destination cards. */
    val fromPricePerPerson: Int
        get() = returnTravelPerPerson + stayPerNight * 3 / 2 + foodPerPersonPerDay * 4
}
