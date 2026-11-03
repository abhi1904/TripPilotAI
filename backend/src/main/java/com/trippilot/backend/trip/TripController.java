package com.trippilot.backend.trip;

import com.trippilot.backend.trip.TripDtos.RecordPaymentRequest;
import com.trippilot.backend.trip.TripDtos.SaveTripRequest;
import com.trippilot.backend.trip.TripDtos.TripResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService service;

    public TripController(TripService service) {
        this.service = service;
    }

    @GetMapping
    public List<TripResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TripResponse get(@PathVariable String id) {
        return service.findById(id);
    }

    @PostMapping
    public TripResponse save(@Valid @RequestBody SaveTripRequest request) {
        return service.save(request.plan());
    }

    @PostMapping("/{id}/payment")
    public TripResponse recordPayment(@PathVariable String id, @Valid @RequestBody RecordPaymentRequest request) {
        return service.recordPayment(id, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        service.deleteAll();
    }
}
