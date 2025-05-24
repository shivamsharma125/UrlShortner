package com.shivam.urlshortenerservice.services;

import com.shivam.urlshortenerservice.models.User;

public interface IAuthService {
    User signUp(String name, String email, String password);
    String login(String email, String password);
}
