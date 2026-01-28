package com.example.url_system.exceptions;

import java.time.LocalDate;

public record ApiResponse(
        String info,
        String message,
        LocalDate time
) {
}
