package com.trippilot.trippilotai

import com.trippilot.trippilotai.data.mock.MockItineraryGenerator
import com.trippilot.trippilotai.data.remote.RemoteTripRepository
import com.trippilot.trippilotai.data.remote.SaveTripBody
import com.trippilot.trippilotai.data.remote.TripDto
import com.trippilot.trippilotai.data.remote.TripPilotApi
import com.trippilot.trippilotai.domain.model.Interest
import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.TripPlan
import com.trippilot.trippilotai.domain.model.TripRequest
import com.trippilot.trippilotai.domain.repository.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteTripRepositoryTest {

    private val server = MockWebServer()
    private val json = TripPilotApi.json
    private val generator = MockItineraryGenerator(delayMillis = 0)
    private lateinit var api: TripPilotApi
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val plan: TripPlan = generator.build(
        TripRequest("Goa", 1_798_761_600_000, 1_799_020_800_000, 2, 40_000, 150_000, setOf(Interest.FOOD)),
        id = "goa-1",
    )

    @Before
    fun setUp() {
        server.start()
        api = TripPilotApi.create(server.url("/").toString())
    }

    @After
    fun tearDown() {
        scope.cancel()
        server.shutdown()
    }

    private fun repository() = RemoteTripRepository(api, scope, samples = { emptyList() }, clock = { 42L })

    private fun tripJson(status: PaymentStatus = PaymentStatus.NOT_PAID, orderId: String? = null) =
        json.encodeToString(TripDto("goa-1", plan, 1_000L, status, orderId, orderId?.let { PaymentMethod.UPI }))

    @Test
    fun loadsTripsFromServerOnStart() {
        server.enqueue(MockResponse().setBody("[${tripJson()}]"))

        val repo = repository()
        val get = server.takeRequest()
        waitFor { repo.syncState.value == SyncState.Idle }

        assertEquals(listOf("goa-1"), repo.trips.value.map { it.id })
        assertEquals(plan, repo.trips.value.single().plan) // full plan survives the JSON round trip
        assertEquals("GET /api/trips HTTP/1.1", get.requestLine)
    }

    @Test
    fun saveShowsTripImmediatelyAndPostsThePlan() {
        server.enqueue(MockResponse().setBody("[]"))
        server.enqueue(MockResponse().setBody(tripJson()))
        val repo = repository()

        repo.saveTrip(plan)
        assertEquals("goa-1", repo.trips.value.single().id) // optimistic update, before the server answers

        server.takeRequest()
        val post = server.takeRequest()
        waitFor { repo.syncState.value == SyncState.Idle }

        assertEquals("POST /api/trips HTTP/1.1", post.requestLine)
        val sent = json.decodeFromString<SaveTripBody>(post.body.readUtf8())
        assertEquals(plan, sent.plan)
        assertEquals(1_000L, repo.trips.value.single().savedAtMillis) // replaced by the server's copy
    }

    @Test
    fun paymentIsSentWithPlanSoUnsavedTripsCanBeCreated() {
        server.enqueue(MockResponse().setBody("[]"))
        server.enqueue(MockResponse().setBody(tripJson(PaymentStatus.PAID, "MOCK-1")))
        val repo = repository()

        repo.recordPayment(plan, PaymentStatus.PAID, PaymentMethod.UPI, "MOCK-1")
        server.takeRequest()
        val post = server.takeRequest()
        waitFor { repo.syncState.value == SyncState.Idle }

        assertEquals("POST /api/trips/goa-1/payment HTTP/1.1", post.requestLine)
        val body = json.parseToJsonElement(post.body.readUtf8()).jsonObject
        assertEquals("PAID", body["status"]!!.jsonPrimitive.content)
        assertEquals("UPI", body["method"]!!.jsonPrimitive.content)
        assertEquals("goa-1", body["plan"]!!.jsonObject["id"]!!.jsonPrimitive.content)
        assertEquals(PaymentStatus.PAID, repo.trips.value.single().paymentStatus)
    }

    @Test
    fun serverErrorIsReportedAndRetryRecovers() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setBody("[${tripJson()}]"))
        val repo = repository()
        server.takeRequest()
        waitFor { repo.syncState.value is SyncState.Error }
        assertTrue((repo.syncState.value as SyncState.Error).message.contains("500"))

        repo.refresh()
        server.takeRequest()
        waitFor { repo.syncState.value == SyncState.Idle }
        assertEquals(1, repo.trips.value.size)
    }

    /** Retrofit completes on OkHttp's thread, so give the result a moment to land. */
    private fun waitFor(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!condition()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for condition" }
            Thread.sleep(10)
        }
    }
}
