package com.trippilot.backend.trip;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, String> {

    /** Most recently saved or paid first, matching the Android My Trips ordering. */
    List<Trip> findAllByOrderByUpdatedAtDesc();
}
