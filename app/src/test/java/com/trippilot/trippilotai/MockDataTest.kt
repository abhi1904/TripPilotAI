package com.trippilot.trippilotai

import com.trippilot.trippilotai.data.mock.InMemoryTripRepository
import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.domain.model.ActivityCategory
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.ui.viewmodel.PlanField
import com.trippilot.trippilotai.ui.viewmodel.PlanTripUiState
import com.trippilot.trippilotai.ui.viewmodel.PlanTripViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockDataTest {

    private val generator = MockItineraryGenerator(delayMillis = 0)
    private val start = 1_798_675_200_000L
    private val day = TripRequest.DAY_MILLIS

    private fun request(days: Int = 4, interests: Set<Interest> = setOf(Interest.FOOD)) =
        TripRequest("Goa", start, start + (days - 1) * day, 2, 20_000, 80_000, interests)

    @Test
    fun itinerary_hasOneEntryPerDay_withArrivalAndDeparture() {
        val plan = generator.build(request(days = 4))
        assertEquals(4, plan.days.size)
        assertEquals("Arrive & check in", plan.days.first().activities.first().title)
        assertEquals("Check out & depart", plan.days.last().activities.last().title)
    }

    @Test
    fun itinerary_prefersSelectedInterests() {
        val plan = generator.build(request(interests = setOf(Interest.CULTURE)))
        val interestActivities = plan.days.flatMap { it.activities }
            .filterNot { it.category == ActivityCategory.TRAVEL || it.title.startsWith("Dinner") }
        assertTrue(interestActivities.all { it.category == ActivityCategory.CULTURE })
    }

    @Test
    fun budget_totalIsSumOfItems() {
        val plan = generator.build(request())
        assertEquals(plan.budget.items.sumOf { it.amount }, plan.budget.total)
        assertEquals(5, plan.budget.items.size)
    }

    @Test
    fun validation_reportsMissingFields() {
        val errors = PlanTripViewModel.validate(PlanTripUiState(destination = " ", interests = emptySet()))
        assertEquals(setOf(PlanField.DESTINATION, PlanField.DATES, PlanField.INTERESTS), errors.keys)
    }

    @Test
    fun validation_rejectsTooLongTrips() {
        val state = PlanTripUiState(destination = "Goa", startDateMillis = start, endDateMillis = start + 20 * day)
        assertEquals(setOf(PlanField.DATES), PlanTripViewModel.validate(state).keys)
    }

    @Test
    fun savingTrip_worksWithoutPayment_andPaymentUpdatesStatus() {
        val repository = InMemoryTripRepository(generator, seedSamples = false)
        val plan = generator.build(request())

        repository.saveTrip(plan)
        assertEquals(PaymentStatus.NOT_PAID, repository.trips.value.single().paymentStatus)

        repository.recordPayment(plan, PaymentStatus.PAID, PaymentMethod.UPI, "MOCK-1")
        val trip = repository.trips.value.single()
        assertEquals(PaymentStatus.PAID, trip.paymentStatus)
        assertEquals("MOCK-1", trip.orderId)
    }
}
