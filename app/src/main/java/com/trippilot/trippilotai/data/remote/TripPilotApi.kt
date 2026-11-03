package com.trippilot.trippilotai.data.remote

import com.trippilot.trippilotai.domain.model.PaymentMethod
import com.trippilot.trippilotai.domain.model.PaymentStatus
import com.trippilot.trippilotai.domain.model.Trip
import com.trippilot.trippilotai.domain.model.TripPlan
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/** The Spring Boot backend's trips API (see backend/README.md). */
interface TripPilotApi {

    @GET("api/trips")
    suspend fun getTrips(): List<TripDto>

    @POST("api/trips")
    suspend fun saveTrip(@Body body: SaveTripBody): TripDto

    @POST("api/trips/{id}/payment")
    suspend fun recordPayment(@Path("id") id: String, @Body body: PaymentBody): TripDto

    @DELETE("api/trips")
    suspend fun deleteAll()

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        fun create(baseUrl: String, client: OkHttpClient = defaultClient()): TripPilotApi =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(TripPilotApi::class.java)

        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}

@Serializable
data class SaveTripBody(val plan: TripPlan)

@Serializable
data class PaymentBody(
    val status: PaymentStatus,
    val method: PaymentMethod,
    val orderId: String? = null,
    /** Lets the server save the trip with the payment if it wasn't saved before. */
    val plan: TripPlan? = null,
)

@Serializable
data class TripDto(
    val id: String,
    val plan: TripPlan,
    val savedAtMillis: Long,
    val paymentStatus: PaymentStatus,
    val orderId: String? = null,
    val paymentMethod: PaymentMethod? = null,
) {
    fun toDomain() = Trip(plan, savedAtMillis, paymentStatus, orderId, paymentMethod)
}
