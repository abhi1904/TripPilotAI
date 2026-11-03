package com.trippilot.backend.trip;

import com.trippilot.backend.common.BadRequestException;
import com.trippilot.backend.common.NotFoundException;
import com.trippilot.backend.trip.TripDtos.RecordPaymentRequest;
import com.trippilot.backend.trip.TripDtos.TripResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class TripService {

    private final TripRepository repository;
    private final JsonMapper jsonMapper;

    public TripService(TripRepository repository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public List<TripResponse> findAll() {
        return repository.findAllByOrderByUpdatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findById(String id) {
        return toResponse(getTrip(id));
    }

    /** Saves a plan. Saving the same plan id again is a no-op, like the Android in-memory repository. */
    @Transactional
    public TripResponse save(JsonNode plan) {
        String id = requiredText(plan, "id");
        return toResponse(repository.findById(id).orElseGet(() -> repository.save(newTrip(id, plan))));
    }

    @Transactional
    public TripResponse recordPayment(String id, RecordPaymentRequest request) {
        Trip trip = repository.findById(id).orElseGet(() -> {
            if (request.plan() == null) {
                throw new NotFoundException("Trip " + id + " not found; include 'plan' to save it with the payment");
            }
            if (!id.equals(requiredText(request.plan(), "id"))) {
                throw new BadRequestException("plan.id does not match the trip id in the URL");
            }
            return repository.save(newTrip(id, request.plan()));
        });
        trip.recordPayment(request.status(), request.method(), request.orderId());
        return toResponse(trip);
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAllInBatch();
    }

    private Trip getTrip(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Trip " + id + " not found"));
    }

    private Trip newTrip(String id, JsonNode plan) {
        JsonNode request = plan.path("request");
        String destination = requiredText(request, "destinationName");
        LocalDate start = toDate(requiredLong(request, "startDateMillis"));
        LocalDate end = toDate(requiredLong(request, "endDateMillis"));
        if (end.isBefore(start)) {
            throw new BadRequestException("plan.request.endDateMillis is before startDateMillis");
        }
        int travellers = (int) requiredLong(request, "travellers");
        return new Trip(id, destination, start, end, travellers, jsonMapper.writeValueAsString(plan));
    }

    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                jsonMapper.readTree(trip.getPlan()),
                trip.getSavedAt().toEpochMilli(),
                trip.getPaymentStatus(),
                trip.getOrderId(),
                trip.getPaymentMethod());
    }

    private static LocalDate toDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static String requiredText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isString() || value.asString().isBlank()) {
            throw new BadRequestException("Missing or empty '" + field + "' in trip plan");
        }
        return value.asString();
    }

    private static long requiredLong(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isIntegralNumber()) {
            throw new BadRequestException("Missing or non-numeric '" + field + "' in trip plan");
        }
        return value.asLong();
    }
}
