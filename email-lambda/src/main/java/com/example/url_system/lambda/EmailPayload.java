package com.example.url_system.lambda;

public record EmailPayload(String toEmail, String subject, String body) {}

