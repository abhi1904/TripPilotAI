package com.trippilot.trippilotai.data

import com.trippilot.trippilotai.BuildConfig
import com.trippilot.trippilotai.data.mock.InMemoryTripRepository
import com.trippilot.trippilotai.data.mock.LocalDestinationSearch
import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.data.mock.MockTravelAssistant
import com.trippilot.trippilotai.data.mock.SimulatedPaymentProcessor
import com.trippilot.trippilotai.data.mock.sampleTrips
import com.trippilot.trippilotai.data.remote.PhotonApi
import com.trippilot.trippilotai.data.remote.PhotonDestinationSearch
import com.trippilot.trippilotai.data.remote.RemoteTripRepository
import com.trippilot.trippilotai.data.remote.TripPilotApi
import com.trippilot.trippilotai.domain.repository.DestinationSearch
import com.trippilot.trippilotai.domain.repository.ItineraryGenerator
import com.trippilot.trippilotai.domain.repository.PaymentProcessor
import com.trippilot.trippilotai.domain.repository.TravelAssistant
import com.trippilot.trippilotai.domain.repository.TripRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency wiring. Saved trips come from the Spring Boot backend when
 * BuildConfig.USE_BACKEND is true (set trippilot.useBackend in local.properties);
 * itinerary generation, chat and payments are still local mocks.
 */
object AppContainer
{

    private val mockGenerator = MockItineraryGenerator()

    /** Lives as long as the app, so background saves aren't cancelled when a screen closes. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val destinationSearch: DestinationSearch by lazy {
        PhotonDestinationSearch(PhotonApi.create(), LocalDestinationSearch())
    }
    val itineraryGenerator: ItineraryGenerator = mockGenerator
    val travelAssistant: TravelAssistant = MockTravelAssistant()
    val paymentProcessor: PaymentProcessor = SimulatedPaymentProcessor()
    val tripRepository: TripRepository by lazy {
        if (BuildConfig.USE_BACKEND) {
            RemoteTripRepository(TripPilotApi.create(BuildConfig.BACKEND_URL), appScope, samples = { sampleTrips(mockGenerator) })
        } else {
            InMemoryTripRepository(mockGenerator)
        }
    }
    val draftStore = TripDraftStore()
}
