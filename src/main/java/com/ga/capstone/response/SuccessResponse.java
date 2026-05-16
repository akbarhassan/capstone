package com.ga.capstone.response;

import java.time.LocalDateTime;

public record SuccessResponse(
        int status,
        String message,
        Object data,
        LocalDateTime timestamp
) {
}
