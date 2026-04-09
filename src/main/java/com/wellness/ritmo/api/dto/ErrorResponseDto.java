package com.wellness.ritmo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDto(
        int status,
        String message,
        Instant timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> errors
) {
    public ErrorResponseDto(int status, String message) {
        this(status, message, Instant.now(), null);
    }
}
