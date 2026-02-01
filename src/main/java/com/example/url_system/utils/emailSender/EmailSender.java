package com.example.url_system.utils.emailSender;

public interface EmailSender {
    void send(String to, String subject, String body);
}
