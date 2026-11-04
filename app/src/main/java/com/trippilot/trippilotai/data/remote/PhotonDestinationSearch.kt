package com.trippilot.trippilotai.data.remote

import com.trippilot.trippilotai.domain.model.DestinationSuggestion
import com.trippilot.trippilotai.domain.repository.DestinationSearch
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/** Photon's public place search, built on OpenStreetMap data (https://photon.komoot.io). */
interface PhotonApi {

    @Headers("User-Agent: TripPilotAI-Android")
    @GET("api/")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("lang") language: String = "en",
        @Query("layer") layers: List<String> = listOf("city", "state", "country"),
    ): PhotonResponse

    companion object {
        const val BASE_URL = "https://photon.komoot.io/"

        fun create(baseUrl: String = BASE_URL, client: OkHttpClient = defaultClient()): PhotonApi =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(TripPilotApi.json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(PhotonApi::class.java)

        // Short timeouts: a slow suggestion is no use to someone who is still typing.
        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .build()
    }
}

@Serializable
data class PhotonResponse(val features: List<PhotonFeature> = emptyList())

@Serializable
data class PhotonFeature(val properties: PhotonProperties)

@Serializable
data class PhotonProperties(
    val name: String? = null,
    val state: String? = null,
    val country: String? = null,
)

/**
 * Live suggestions after the built-in destinations in [local], which come first because
 * they have detailed itineraries. Falls back to [local] alone when Photon can't be reached.
 */
class PhotonDestinationSearch(
    private val api: PhotonApi,
    private val local: DestinationSearch,
) : DestinationSearch {

    override suspend fun search(query: String): List<DestinationSuggestion> {
        val known = local.search(query)
        val text = query.trim()
        if (text.length < MIN_QUERY_LENGTH) return known

        val live = try {
            api.search(text, MAX_RESULTS).features.mapNotNull { it.properties.toSuggestion() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return known
        }
        val knownNames = known.map { it.name.lowercase() }.toSet()
        return (known + live.filter { it.name.lowercase() !in knownNames }).distinct().take(MAX_RESULTS)
    }

    private fun PhotonProperties.toSuggestion(): DestinationSuggestion? {
        val placeName = name?.takeIf { it.isNotBlank() } ?: return null
        val region = listOfNotNull(state, country).filter { it != placeName }.distinct().joinToString(", ")
        return DestinationSuggestion(placeName, region)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_RESULTS = 6
    }
}
