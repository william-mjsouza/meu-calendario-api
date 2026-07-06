package com.william.meu_calendario_api.service;

import com.william.meu_calendario_api.dto.auth.AuthResponse;
import com.william.meu_calendario_api.dto.auth.LoginRequest;
import com.william.meu_calendario_api.dto.auth.RegisterRequest;
import com.william.meu_calendario_api.entity.User;
import com.william.meu_calendario_api.exception.BusinessException;
import com.william.meu_calendario_api.repository.UserRepository;
import com.william.meu_calendario_api.security.CustomUserDetails;
import com.william.meu_calendario_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe uma conta com esse e-mail");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .build();
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(new CustomUserDetails(saved));
        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // Delega a validação de credenciais para o AuthenticationManager (BCrypt + UserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, userDetails.getId(), userDetails.getName(), userDetails.getUsername());
    }
}
