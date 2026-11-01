package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.Activity
import com.trippilot.trippilotai.domain.model.ActivityCategory
import com.trippilot.trippilotai.domain.model.BudgetCategory
import com.trippilot.trippilotai.domain.model.BudgetEstimate
import com.trippilot.trippilotai.domain.model.BudgetItem
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.ItineraryDay
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.domain.repository.ItineraryGenerator
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Builds a representative itinerary from local templates. No AI service is called;
 * the delay only makes the loading state visible.
 */
class MockItineraryGenerator(private val delayMillis: Long = 1_200) : ItineraryGenerator {

    override suspend fun generate(request: TripRequest): TripPlan {
        delay(delayMillis)
        return build(request)
    }

    fun build(request: TripRequest, id: String = UUID.randomUUID().toString()): TripPlan {
        val destination = MockDestinations.findOrGeneric(request.destinationName)
        val dayCount = request.dayCount.coerceIn(1, MAX_DAYS)
        val travellers = request.travellers
        val rooms = (travellers + 1) / 2

        val pool = destination.activities
            .filter { it.interest in request.interests }
            .ifEmpty { destination.activities }
        var next = 0
        fun nextActivity(time: String): Activity {
            val template = pool[next++ % pool.size]
            return Activity(time, template.title, template.description, template.interest.toCategory(), template.costPerPerson)
        }

        val days = (1..dayCount).map { dayNumber ->
            val isFirst = dayNumber == 1
            val isLast = dayNumber == dayCount && dayCount > 1
            val activities = buildList {
                when {
                    isFirst -> {
                        add(Activity("11:00", "Arrive & check in", "Transfer to your stay in ${destination.name} and settle in.", ActivityCategory.TRAVEL, 0))
                        add(nextActivity("15:30"))
                    }
                    isLast -> {
                        add(nextActivity("09:00"))
                    }
                    else -> {
                        add(nextActivity("09:00"))
                        add(nextActivity("12:30"))
                        add(nextActivity("16:00"))
                    }
                }
                if (isLast) {
                    add(Activity("13:00", "Check out & depart", "Pack up, last souvenirs and head home.", ActivityCategory.TRAVEL, 0))
                } else {
                    add(Activity("19:30", "Dinner at a local favourite", "Regional dishes recommended for ${destination.name}.", ActivityCategory.FOOD, 0))
                }
            }
            val stay = if (isLast) 0 else destination.stayPerNight * rooms
            val cost = activities.sumOf { it.costPerPerson } * travellers +
                destination.foodPerPersonPerDay * travellers +
                destination.localTransportPerDay + stay
            ItineraryDay(
                dayNumber = dayNumber,
                dateMillis = request.startDateMillis + (dayNumber - 1) * TripRequest.DAY_MILLIS,
                title = when {
                    isFirst -> "Arrival & first impressions"
                    isLast -> "Farewell ${destination.name}"
                    else -> activities.first().category.dayTitle()
                },
                activities = activities,
                estimatedCost = cost,
            )
        }

        val nights = (dayCount - 1).coerceAtLeast(1)
        val accommodation = destination.stayPerNight * rooms * nights
        val transport = destination.returnTravelPerPerson * travellers + destination.localTransportPerDay * dayCount
        val activitiesCost = days.sumOf { day -> day.activities.sumOf { it.costPerPerson } } * travellers
        val food = destination.foodPerPersonPerDay * travellers * dayCount
        val other = (accommodation + transport + activitiesCost + food) * 7 / 100

        val budget = BudgetEstimate(
            listOf(
                BudgetItem(BudgetCategory.ACCOMMODATION, accommodation, "$nights night${if (nights > 1) "s" else ""} · $rooms room${if (rooms > 1) "s" else ""}"),
                BudgetItem(BudgetCategory.TRANSPORT, transport, "Return travel + local transfers"),
                BudgetItem(BudgetCategory.ACTIVITIES, activitiesCost, "Entry fees, tours and experiences"),
                BudgetItem(BudgetCategory.FOOD, food, "Meals for $travellers traveller${if (travellers > 1) "s" else ""}"),
                BudgetItem(BudgetCategory.OTHER, other, "Insurance, tips and buffer (~7%)"),
            ),
        )
        return TripPlan(id, request.copy(destinationName = destination.name), destination, days, budget)
    }

    private fun Interest.toCategory() = when (this) {
        Interest.NATURE -> ActivityCategory.NATURE
        Interest.CULTURE -> ActivityCategory.CULTURE
        Interest.FOOD -> ActivityCategory.FOOD
        Interest.ADVENTURE -> ActivityCategory.ADVENTURE
        Interest.RELAXATION -> ActivityCategory.RELAXATION
        Interest.SHOPPING -> ActivityCategory.SHOPPING
    }

    private fun ActivityCategory.dayTitle() = when (this) {
        ActivityCategory.NATURE -> "Into the outdoors"
        ActivityCategory.CULTURE -> "Heritage & culture"
        ActivityCategory.FOOD -> "A day of local flavours"
        ActivityCategory.ADVENTURE -> "Adventure day"
        ActivityCategory.RELAXATION -> "Slow & easy"
        ActivityCategory.SHOPPING -> "Markets & souvenirs"
        ActivityCategory.TRAVEL, ActivityCategory.STAY -> "Exploring"
    }

    companion object {
        const val MAX_DAYS = 14
    }
}
