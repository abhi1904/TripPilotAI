-- Saved trips. The full itinerary/budget plan is stored as JSON (same shape as the Android TripPlan),
-- with the fields we filter or sort on pulled out into their own columns.
CREATE TABLE trips (
    id               VARCHAR(64)  PRIMARY KEY,
    destination_name VARCHAR(120) NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    travellers       INTEGER      NOT NULL,
    plan             JSONB        NOT NULL,
    payment_status   VARCHAR(20)  NOT NULL,
    payment_method   VARCHAR(20),
    order_id         VARCHAR(64),
    saved_at         TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_trips_updated_at ON trips (updated_at DESC);
