package com.shivam.urlshortenerservice.controllers;

import com.shivam.urlshortenerservice.dtos.ClickStatsResponse;
import com.shivam.urlshortenerservice.dtos.TopClickedResponse;
import com.shivam.urlshortenerservice.models.ClickEvent;
import com.shivam.urlshortenerservice.services.IAnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "shivam@gmail.com")
public class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAnalyticsService analyticsService;

    @Test
    @DisplayName("Should return click count successfully")
    void test_GetClickCount_ValidShortCode_ReturnsClickCount() throws Exception {
        when(analyticsService.getClickCount("abc123", "shivam@gmail.com")).thenReturn(15L);

        mockMvc.perform(get("/analytics/abc123/click-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode", is("abc123")))
                .andExpect(jsonPath("$.totalClicks", is(15)));
    }

    @Test
    @DisplayName("Should return filtered click events successfully")
    void test_GetFilteredClickEvents_ValidFilters_ReturnsPage() throws Exception {
        ClickEvent event = new ClickEvent("192.168.0.1", "Chrome", "Windows", "Desktop", "google.com", new Date(), null);
        Page<ClickEvent> page = new PageImpl<>(List.of(event));
        when(analyticsService.getFilteredClickEvents(eq("abc123"), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/analytics/abc123/click-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].browser", is("Chrome")));
    }

    @Test
    @DisplayName("Should return empty list when no click events match filters")
    void test_GetFilteredClickEvents_NoEvents_ReturnsEmptyPage() throws Exception {
        Page<ClickEvent> page = new PageImpl<>(Collections.emptyList());
        when(analyticsService.getFilteredClickEvents(eq("abc123"), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/analytics/abc123/click-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should return daily click stats successfully")
    void test_GetDailyClickStats_ValidShortCode_ReturnsStats() throws Exception {
        List<ClickStatsResponse> stats = List.of(new ClickStatsResponse("abc123", "2025-05-01", 10));
        when(analyticsService.getDailyClickStats("abc123", "shivam@gmail.com")).thenReturn(stats);

        mockMvc.perform(get("/analytics/abc123/click-events/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode", is("abc123")))
                .andExpect(jsonPath("$[0].count", is(10)));
    }

    @Test
    @DisplayName("Should return stats in range successfully")
    void test_GetClickStatsInRange_ValidDates_ReturnsStats() throws Exception {
        List<ClickStatsResponse> stats = List.of(new ClickStatsResponse("abc123", "2025-05-01", 5));
        when(analyticsService.getStatsInDateRange(eq("abc123"), eq("2025-05-01"), eq("2025-05-10"), eq("shivam@gmail.com"))).thenReturn(stats);

        mockMvc.perform(get("/analytics/abc123/click-events/range?startDate=2025-05-01&endDate=2025-05-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].count", is(5)));
    }

    @Test
    @DisplayName("Should return bad request when startDate is after endDate")
    void test_GetClickStatsInRange_InvalidStartOrEnd_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/analytics/abc123/click-events/range?startDate=2025-05-01&endDate=2025-04-24"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return top clicked URLs for admin access")
    void test_GetTopClickedUrls_AdminAccess_ReturnsList() throws Exception {
        List<TopClickedResponse> response = List.of(new TopClickedResponse("xyz789", 100));
        when(analyticsService.getTopClickedUrls(5)).thenReturn(response);

        mockMvc.perform(get("/analytics/admin/top-clicked?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shortCode", is("xyz789")))
                .andExpect(jsonPath("$[0].count", is(100)));
    }

    @Test
    @DisplayName("Should return not found when invalid shortcode is used")
    void test_GetClickCount_InvalidShortCode_ReturnsNotFound() throws Exception {
        when(analyticsService.getClickCount("invalid", "shivam@gmail.com"))
                .thenThrow(new RuntimeException("Shortcode not found"));

        mockMvc.perform(get("/analytics/invalid/click-count"))
                .andExpect(status().isInternalServerError()); // change to .isNotFound() when you use a custom NotFoundException
    }

    @Test
    @DisplayName("Should return unauthorized if user is not authenticated")
    void test_GetClickCount_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/analytics/abc123/click-count")
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }
}


