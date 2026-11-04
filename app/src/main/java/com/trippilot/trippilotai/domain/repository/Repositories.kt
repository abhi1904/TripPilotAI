package com.trippilot.trippilotai.domain.repository

import com.trippilot.trippilotai.domain.model.DestinationSuggestion
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest
import kotlinx.coroutines.flow.StateFlow

// Phase 1 ships mock implementations of these interfaces (see data/mock).
// Later phases can swap in backend/AI-backed implementations without touching the UI.

interface DestinationSearch {
    suspend fun search(query: String): List<DestinationSuggestion>
}

interface ItineraryGenerator {
    suspend fun generate(request: TripRequest): TripPlan
}

interface TravelAssistant {
    suspend fun reply(message: String, plan: TripPlan, selectedDay: Int): String
}

/** Whether saved trips are in sync with where they're stored (the backend, or memory for the mock). */
sealed interface SyncState {
    data object Idle : SyncState
    data object Loading : SyncState
    data class Error(val message: String) : SyncState
}

/**
 * Saved trips. [trips] updates immediately when something is saved or paid;
 * a backend-backed implementation then syncs in the background and reports problems via [syncState].
 */
interface TripRepository {
    val trips: StateFlow<List<Trip>>
    val syncState: StateFlow<SyncState>
    /** Reloads trips from storage (no-op for in-memory storage). */
    fun refresh()
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
