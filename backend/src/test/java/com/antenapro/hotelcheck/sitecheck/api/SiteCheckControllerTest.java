package com.antenapro.hotelcheck.sitecheck.api;

import com.antenapro.hotelcheck.sitecheck.HostLimitExceededException;
import com.antenapro.hotelcheck.sitecheck.SiteCheckService;
import com.antenapro.hotelcheck.sitecheck.fetch.InvalidUrlException;
import com.antenapro.hotelcheck.sitecheck.model.SiteCheckRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SiteCheckControllerTest {

    private final SiteCheckService service = Mockito.mock(SiteCheckService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SiteCheckController(service)).build();
    }

    @Test
    void start_ShouldAnswer202WithTheRunId() throws Exception {
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), Instant.now());
        when(service.start("hotel.example.com")).thenReturn(run);

        mvc.perform(post("/api/site-checks").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"hotel.example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value(run.id().toString()));
    }

    @Test
    void start_ShouldAnswer400WithTheReason_ForAUrlThePolicyRejects() throws Exception {
        when(service.start(any())).thenThrow(new InvalidUrlException("Local or internal addresses cannot be checked."));

        mvc.perform(post("/api/site-checks").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"http://127.0.0.1:8080\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Local or internal")));
    }

    @Test
    void start_ShouldAnswer429_WhenTheHostHitItsDailyLimit() throws Exception {
        when(service.start(any())).thenThrow(new HostLimitExceededException("hotel.example.com", 3));

        mvc.perform(post("/api/site-checks").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"hotel.example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value(containsString("3 times today")));
    }

    @Test
    void get_ShouldAnswer404_ForAnUnknownOrMalformedRunId() throws Exception {
        when(service.get(any())).thenReturn(Optional.empty());

        mvc.perform(get("/api/site-checks/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/site-checks/not-a-uuid")).andExpect(status().isNotFound());
    }

    @Test
    void get_ShouldAnswerTheRunningRun_WithStatusAndNoFindingsYet() throws Exception {
        SiteCheckRun run = new SiteCheckRun(UUID.randomUUID(), URI.create("https://hotel.example.com/"), Instant.now());
        when(service.get(run.id())).thenReturn(Optional.of(run));

        mvc.perform(get("/api/site-checks/" + run.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.requestedUrl").value("https://hotel.example.com/"))
                .andExpect(jsonPath("$.findings").isEmpty());
    }
}
