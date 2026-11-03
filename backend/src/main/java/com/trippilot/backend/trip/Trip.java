package com.trippilot.backend.trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    private String id;

    @Column(name = "destination_name", nullable = false)
    private String destinationName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int travellers;

    /** The whole TripPlan as JSON text, stored in a JSONB column. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Trip() {
        // for JPA
    }

    public Trip(String id, String destinationName, LocalDate startDate, LocalDate endDate, int travellers, String plan) {
        this.id = id;
        this.destinationName = destinationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travellers = travellers;
        this.plan = plan;
        this.paymentStatus = PaymentStatus.NOT_PAID;
        this.savedAt = Instant.now();
        this.updatedAt = this.savedAt;
    }

    public void recordPayment(PaymentStatus status, PaymentMethod method, String orderId) {
        this.paymentStatus = status;
        this.paymentMethod = method;
        if (orderId != null) {
            this.orderId = orderId;
        }
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getDestinationName() { return destinationName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getTravellers() { return travellers; }
    public String getPlan() { return plan; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getOrderId() { return orderId; }
    public Instant getSavedAt() { return savedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
