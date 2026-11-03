package com.trippilot.trippilotai.data

import com.trippilot.trippilotai.data.mock.InMemoryTripRepository
import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.data.mock.MockTravelAssistant
import com.trippilot.trippilotai.data.mock.SimulatedPaymentProcessor
import com.trippilot.trippilotai.domain.repository.ItineraryGenerator
import com.trippilot.trippilotai.domain.repository.PaymentProcessor
import com.trippilot.trippilotai.domain.repository.TravelAssistant
import com.trippilot.trippilotai.domain.repository.TripRepository

/**
 * Manual dependency wiring for Phase 1. Every dependency is a local mock;
 * swap implementations here when real data sources arrive.
 */
object AppContainer
{

    private val mockGenerator = MockItineraryGenerator()

    val itineraryGenerator: ItineraryGenerator = mockGenerator
    val travelAssistant: TravelAssistant = MockTravelAssistant()
    val paymentProcessor: PaymentProcessor = SimulatedPaymentProcessor()
    val tripRepository: TripRepository = InMemoryTripRepository(mockGenerator)
    val draftStore = TripDraftStore()
}
