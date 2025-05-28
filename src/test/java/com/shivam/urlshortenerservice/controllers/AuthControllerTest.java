package com.shivam.urlshortenerservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivam.urlshortenerservice.dtos.LoginRequest;
import com.shivam.urlshortenerservice.dtos.SignUpRequest;
import com.shivam.urlshortenerservice.exceptions.InvalidCredentialsException;
import com.shivam.urlshortenerservice.exceptions.InvalidRequestException;
import com.shivam.urlshortenerservice.models.Role;
import com.shivam.urlshortenerservice.models.User;
import com.shivam.urlshortenerservice.services.IAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAuthService authService;

    @Test
    @DisplayName("Should register user successfully when valid details are provided")
    void test_Signup_RunsSuccessfully() throws Exception {
        SignUpRequest request = new SignUpRequest("Shivam Sharma", "shivam@gmail.com", "shivam123", Set.of("USER"));

        User user = new User();
        user.setId(1L);
        user.setName("Shivam Sharma");
        user.setEmail("shivam@gmail.com");
        user.setRoles(Set.of(new Role("USER")));

        when(authService.signUp(anyString(), anyString(), anyString(), anySet())).thenReturn(user);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Shivam Sharma")))
                .andExpect(jsonPath("$.email", is("shivam@gmail.com")))
                .andExpect(jsonPath("$.roles", hasItem("USER")));
    }

    @Test
    @DisplayName("Should return bad request when password is null during signup")
    void test_Signup_CalledWithNullPassword_ResultsInBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("Shivam Sharma", "shivam@gmail.com", null, Set.of("USER"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when roles are empty during signup")
    void test_Signup_CalledWithEmptyRoles_ResultsInBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("Shivam Sharma", "shivam@gmail.com", "shivam123", Collections.emptySet());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when email is empty during signup")
    void test_Signup_CalledWithEmptyEmail_ResultsInBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("Shivam Sharma", "", "shivam123", Set.of("USER"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when name is empty during signup")
    void test_Signup_CalledWithEmptyName_ResultsInBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("", "shivam@gmail.com", "shivam123", Set.of("USER"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when email is already registered during signup")
    void test_Signup_CalledWithDuplicateEmail_ResultsInBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("Shivam Sharma", "shivam@gmail.com", "shivam123", Set.of("USER"));

        when(authService.signUp(anyString(), anyString(), anyString(), anySet()))
                .thenThrow(new InvalidRequestException("Email already registered"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return token successfully when login credentials are valid")
    void test_Login_RunsSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivam@gmail.com");
        request.setPassword("shivam123");

        when(authService.login(eq("shivam@gmail.com"), eq("shivam123"))).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mocked-jwt-token")));
    }

    @Test
    @DisplayName("Should return bad request when email is empty during login")
    void test_Login_CalledWithEmptyEmail_ResultsInBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("shivam123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when password is empty during login")
    void test_Login_CalledWithEmptyPassword_ResultsInBadRequest() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivam@gmail.com");
        request.setPassword("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return unauthorized when login credentials are invalid")
    void test_Login_CalledWithInvalidCredentials_ResultsInUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivam@gmail.com");
        request.setPassword("wrongpass");

        when(authService.login(eq("shivam@gmail.com"), eq("wrongpass")))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}



