package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.models.User;

import java.util.Set;

public interface IAuthService {
    User signUp(String name, String email, String password, Set<String> rolesStr);
    String login(String email, String password);
}
