package com.trippilot.trippilotai.data.mock

import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.repository.TravelAssistant
import kotlinx.coroutines.delay

/** Keyword-based canned replies. No LLM or MCP calls; the itinerary is not modified. */
class MockTravelAssistant(private val delayMillis: Long = 900) : TravelAssistant {

    override suspend fun reply(message: String, plan: TripPlan, selectedDay: Int): String {
        delay(delayMillis)
        val text = message.lowercase()
        val destination = plan.destination
        val day = plan.days.getOrNull(selectedDay - 1)
        return when {
            listOf("cheap", "budget", "cost", "save", "expensive").any { it in text } -> {
                val priciest = plan.days.flatMap { it.activities }.maxByOrNull { it.costPerPerson }
                "To trim costs I'd swap \"${priciest?.title ?: "the priciest activity"}\" for a free alternative " +
                    "and choose a homestay over a hotel. That could save roughly 10–15% of the estimate."
            }
            listOf("relax", "slow", "rest", "tired", "easy").any { it in text } ->
                "Sure — I'd keep only the morning plan on Day $selectedDay and leave the afternoon free. " +
                    "A relaxed café or spa break nearby would fit well."
            listOf("food", "eat", "restaurant", "dinner", "lunch").any { it in text } -> {
                val foodIdea = destination.activities.firstOrNull { it.interest.name == "FOOD" }
                "For food lovers in ${destination.name}, try \"${foodIdea?.title ?: "a local food walk"}\". " +
                    "I'd slot it in for lunch on Day $selectedDay."
            }
            listOf("rain", "weather", "monsoon", "indoor").any { it in text } ->
                "If it rains, swap outdoor stops for museums, a cooking class or a spa session. " +
                    "Day $selectedDay has ${day?.activities?.size ?: 0} stops — I'd move outdoor ones to a drier day."
            listOf("add", "more", "another", "extra").any { it in text } ->
                "You could add \"${destination.activities.last().title}\" to Day $selectedDay. " +
                    "It pairs nicely with the existing plan."
            else ->
                "Got it! In a later phase I'll be able to update your ${destination.name} itinerary directly. " +
                    "For now, try asking for cheaper options, a slower day, food ideas or a rainy-day plan."
        }
    }
}
