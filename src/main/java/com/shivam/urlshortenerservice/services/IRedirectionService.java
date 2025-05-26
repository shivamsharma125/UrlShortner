package com.shivam.urlshortenerservice.services;

public interface IRedirectionService {
    String getOriginalUrl(String shortCode);
}
