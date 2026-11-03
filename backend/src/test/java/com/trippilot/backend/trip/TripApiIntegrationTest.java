package com.trippilot.backend.trip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Runs the real app (Flyway migrations, JPA, JSONB) against a throwaway local PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
class TripApiIntegrationTest {

    private static final EmbeddedPostgres POSTGRES = startPostgres();

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        POSTGRES.close();
    }

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void clean() throws Exception {
        mvc.perform(delete("/api/trips")).andExpect(status().isNoContent());
    }

    @Test
    void savesListsAndReadsATrip() throws Exception {
        save(planJson("goa-1", "Goa")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("goa-1"))
                .andExpect(jsonPath("$.paymentStatus").value("NOT_PAID"))
                .andExpect(jsonPath("$.plan.request.travellers").value(2))
                .andExpect(jsonPath("$.plan.days[0].activities[0].title").value("Beach walk"));

        mvc.perform(get("/api/trips")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].plan.destination.name").value("Goa"));

        mvc.perform(get("/api/trips/goa-1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.plan.budget.items[0].category").value("ACCOMMODATION"));
    }

    @Test
    void savingTheSamePlanTwiceKeepsOneTrip() throws Exception {
        save(planJson("goa-1", "Goa")).andExpect(status().isOk());
        save(planJson("goa-1", "Goa")).andExpect(status().isOk());

        mvc.perform(get("/api/trips")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void recordsPaymentForSavedTripAndMovesItToTheTop() throws Exception {
        save(planJson("goa-1", "Goa")).andExpect(status().isOk());
        save(planJson("manali-1", "Manali")).andExpect(status().isOk());

        pay("goa-1", """
                {"status":"PAID","method":"UPI","orderId":"MOCK-123456"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentMethod").value("UPI"))
                .andExpect(jsonPath("$.orderId").value("MOCK-123456"));

        mvc.perform(get("/api/trips"))
                .andExpect(jsonPath("$[0].id").value("goa-1"))
                .andExpect(jsonPath("$[1].id").value("manali-1"));
    }

    @Test
    void paymentWithPlanSavesAnUnsavedTrip() throws Exception {
        pay("jaipur-1", """
                {"status":"FAILED","method":"CARD","plan":%s}""".formatted(planJson("jaipur-1", "Jaipur")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("FAILED"))
                .andExpect(jsonPath("$.plan.destination.name").value("Jaipur"));
    }

    @Test
    void paymentForUnknownTripWithoutPlanIs404() throws Exception {
        pay("nope", """
                {"status":"PAID","method":"UPI"}""").andExpect(status().isNotFound());
        mvc.perform(get("/api/trips/nope")).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidBodies() throws Exception {
        postTrip("{}").andExpect(status().isBadRequest()); // no plan
        postTrip("{\"plan\":{\"id\":\"x\",\"request\":{}}}").andExpect(status().isBadRequest()); // incomplete plan
        pay("goa-1", "{\"status\":\"PAID\"}").andExpect(status().isBadRequest());
    }

    private ResultActions save(String planJson) throws Exception {
        return postTrip("{\"plan\":" + planJson + "}");
    }

    private ResultActions postTrip(String body) throws Exception {
        return mvc.perform(post("/api/trips").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions pay(String id, String body) throws Exception {
        return mvc.perform(post("/api/trips/{id}/payment", id).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    /** A trimmed-down TripPlan in the same JSON shape the Android app uses. */
    private static String planJson(String id, String destination) {
        return """
                {
                  "id": "%s",
                  "request": {
                    "destinationName": "%s",
                    "startDateMillis": 1798761600000,
                    "endDateMillis": 1799020800000,
                    "travellers": 2,
                    "budgetMin": 40000,
                    "budgetMax": 150000,
                    "interests": ["RELAXATION", "FOOD"]
                  },
                  "destination": {"id": "%s", "name": "%s", "region": "India"},
                  "days": [
                    {"dayNumber": 1, "dateMillis": 1798761600000, "title": "Arrival",
                     "activities": [{"time": "17:00", "title": "Beach walk", "description": "Sunset",
                                     "category": "RELAXATION", "costPerPerson": 0}],
                     "estimatedCost": 9000}
                  ],
                  "budget": {"items": [{"category": "ACCOMMODATION", "amount": 24000, "note": "3 nights"}]}
                }
                """.formatted(id, destination, destination.toLowerCase(), destination);
    }

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not start embedded PostgreSQL", e);
        }
    }
}
