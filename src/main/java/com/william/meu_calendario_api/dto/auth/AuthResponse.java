package com.william.meu_calendario_api.dto.auth;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email
) {}
