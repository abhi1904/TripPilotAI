package com.trippilot.backend.trip;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

/** Request and response bodies for the trips API. JSON field names match the Android models. */
public final class TripDtos {

    private TripDtos() {
    }

    /** Body of POST /api/trips: the Android TripPlan, as JSON. */
    public record SaveTripRequest(@NotNull JsonNode plan) {
    }

    /**
     * Body of POST /api/trips/{id}/payment. {@code plan} is only needed when the trip
     * has not been saved yet (the app lets people pay without saving first).
     */
    public record RecordPaymentRequest(
            @NotNull PaymentStatus status,
            @NotNull PaymentMethod method,
            String orderId,
            JsonNode plan) {
    }

    /** Mirrors the Android Trip model. */
    public record TripResponse(
            String id,
            JsonNode plan,
            long savedAtMillis,
            PaymentStatus paymentStatus,
            String orderId,
            PaymentMethod paymentMethod) {
    }
}
