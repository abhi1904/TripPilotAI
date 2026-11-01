package com.trippilot.trippilotai.domain.model

/** What the traveller asked for on the Plan Trip screen. Dates are UTC-midnight epoch millis. */
data class TripRequest(
    val destinationName: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val travellers: Int,
    val budgetMin: Int,
    val budgetMax: Int,
    val interests: Set<Interest>,
) {
    val dayCount: Int
        get() = ((endDateMillis - startDateMillis) / DAY_MILLIS).toInt() + 1

    companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}

enum class ActivityCategory { TRAVEL, STAY, NATURE, CULTURE, FOOD, ADVENTURE, RELAXATION, SHOPPING }

data class Activity(
    val time: String,
    val title: String,
    val description: String,
    val category: ActivityCategory,
    val costPerPerson: Int,
)

data class ItineraryDay(
    val dayNumber: Int,
    val dateMillis: Long,
    val title: String,
    val activities: List<Activity>,
    /** Estimated spend for the whole group on this day (stay, food, local transport, activities). */
    val estimatedCost: Int,
)

enum class BudgetCategory(val label: String) {
    ACCOMMODATION("Accommodation"),
    TRANSPORT("Transport"),
    ACTIVITIES("Activities"),
    FOOD("Food"),
    OTHER("Other"),
}

data class BudgetItem(val category: BudgetCategory, val amount: Int, val note: String)

data class BudgetEstimate(val items: List<BudgetItem>) {
    val total: Int get() = items.sumOf { it.amount }
}

/** A generated (mock) plan: the request, its day-by-day itinerary and its cost estimate. */
data class TripPlan(
    val id: String,
    val request: TripRequest,
    val destination: Destination,
    val days: List<ItineraryDay>,
    val budget: BudgetEstimate,
)
