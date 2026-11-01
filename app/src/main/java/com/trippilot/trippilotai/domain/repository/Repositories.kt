package com.trippilot.trippilotai.domain.repository

import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest
import kotlinx.coroutines.flow.StateFlow

// Phase 1 ships mock implementations of these interfaces (see data/mock).
// Later phases can swap in backend/AI-backed implementations without touching the UI.

interface ItineraryGenerator {
    suspend fun generate(request: TripRequest): TripPlan
}

interface TravelAssistant {
    suspend fun reply(message: String, plan: TripPlan, selectedDay: Int): String
}

interface TripRepository {
    val trips: StateFlow<List<Trip>>
    fun saveTrip(plan: TripPlan)
    fun recordPayment(plan: TripPlan, status: PaymentStatus, method: PaymentMethod, orderId: String?)
    fun clearAll()
    fun restoreSamples()
}

sealed interface PaymentResult {
    data class Success(val orderId: String) : PaymentResult
    data class Failed(val message: String) : PaymentResult
}

/** Outcome the simulated processor should produce; chosen from a debug-only control. */
enum class SimulatedOutcome { SUCCESS, FAILURE }

interface PaymentProcessor {
    suspend fun pay(amount: Int, method: PaymentMethod, outcome: SimulatedOutcome): PaymentResult
}
