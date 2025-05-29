package com.shivam.urlshortenerservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.urlshortenerservice.dtos.ShortUrlRequest;
import com.shivam.urlshortenerservice.models.ShortUrl;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.services.IAnalyticsService;
import com.shivam.urlshortenerservice.services.IShortUrlService;
import com.shivam.urlshortenerservice.utils.ShortUrlUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
@AutoConfigureMockMvc
@WithMockUser(username = "shivam@gmail.com", roles = "USER")
public class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IShortUrlService shortUrlService;

    @MockBean
    private IAnalyticsService analyticsService;

    private final String email = "shivam@gmail.com";
    private final String originalUrl = "https://www.google.com";
    private final String customAlias = "ggle";
    private final String shortUrlCustomAlias = ShortUrlUtil.baseUrl + customAlias;

    @Test
    @DisplayName("Should create short URL successfully")
    void test_CreateShortUrl_Success() throws Exception {
        ShortUrlRequest request = new ShortUrlRequest(originalUrl, customAlias, null);
        ShortUrl mockShortUrl = new ShortUrl();
        mockShortUrl.setOriginalUrl(originalUrl);
        mockShortUrl.setShortCode(customAlias);
        mockShortUrl.setExpiresAt(new Date(System.currentTimeMillis() +15 * 24 * 60 * 60 * 1000L));
        mockShortUrl.setCreatedBy(new User());

        when(shortUrlService.createShortUrl(eq(originalUrl), eq(customAlias), any(), eq(email)))
                .thenReturn(mockShortUrl);

        mockMvc.perform(post("/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl", is(shortUrlCustomAlias)))
                .andExpect(jsonPath("$.originalUrl", is(originalUrl)));
    }

    @Test
    @DisplayName("Should return 400 for empty original URL")
    void test_CreateShortUrl_EmptyOriginalUrl_ReturnsBadRequest() throws Exception {
        ShortUrlRequest request = new ShortUrlRequest("", customAlias, null);

        mockMvc.perform(post("/shorten")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should redirect to original URL and log click")
    void test_RedirectToOriginalUrl_Success() throws Exception {
        when(shortUrlService.getOriginalUrl("ggle")).thenReturn(originalUrl);

        mockMvc.perform(get("/ggle")
                        .header("User-Agent", "mozilla")
                        .header("Referer", "https://parent.google.com")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(analyticsService, times(1)).logClick(eq("ggle"), eq("127.0.0.1"), eq("mozilla"), eq("https://parent.google.com"));
    }

    @Test
    @DisplayName("Should return 500 for empty short code in redirect")
    void test_RedirectToOriginalUrl_EmptyShortCode_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get user's short URL by shortCode")
    void test_GetShortUrlByUser_Success() throws Exception {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setShortCode(customAlias);
        shortUrl.setExpiresAt(new Date());
        shortUrl.setCreatedBy(new User());

        when(shortUrlService.getShortUrl(customAlias, email)).thenReturn(shortUrl);

        mockMvc.perform(get("/shorten/ggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl", is(shortUrlCustomAlias)))
                .andExpect(jsonPath("$.originalUrl", is(originalUrl)));
    }

    @Test
    @DisplayName("Should delete user's short URL")
    void test_DeleteShortUrlByUser_Success() throws Exception {
        mockMvc.perform(delete("/shorten/ggle")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(shortUrlService, times(1)).deleteShortUrl(customAlias, email);
    }

    @Test
    @DisplayName("Should return 500 for empty short code in delete")
    void test_DeleteShortUrlByUser_EmptyShortCode_ReturnsInternalServerError() throws Exception {
        mockMvc.perform(delete("/shorten/")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should get all short URLs for user")
    void test_GetUserShortUrls_Success() throws Exception {
        Page<ShortUrl> page = new PageImpl<>(List.of(new ShortUrl()));
        when(shortUrlService.getShortUrlsForUser(eq(email), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/shorten/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should return all URLs to user with ADMIN role")
    @WithMockUser(username = "shivam@gmail.com", roles = {"ADMIN"})
    void test_GetAllUrls_AdminAccess_Success() throws Exception {
        Page<ShortUrl> page = new PageImpl<>(List.of(new ShortUrl()));
        when(shortUrlService.getAllShortUrls(eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/shorten/admin/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should allow admin to delete any short URL")
    @WithMockUser(username = "shivam@gmail.com", roles = {"ADMIN"})
    void test_DeleteShortUrlAsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/shorten/admin/ggle")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(shortUrlService, times(1)).deleteShortUrlAsAdmin(eq(customAlias), eq(email));
    }
}
