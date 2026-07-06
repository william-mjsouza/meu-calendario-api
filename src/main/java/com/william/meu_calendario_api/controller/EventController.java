package com.william.meu_calendario_api.controller;

import com.william.meu_calendario_api.dto.event.EventRequest;
import com.william.meu_calendario_api.dto.event.EventResponse;
import com.william.meu_calendario_api.security.CustomUserDetails;
import com.william.meu_calendario_api.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /** Lista todos os eventos do usuário autenticado. */
    @GetMapping
    public ResponseEntity<List<EventResponse>> list(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(eventService.listByUser(user.getId()));
    }

    /** Cria um novo evento para o usuário autenticado. */
    @PostMapping
    public ResponseEntity<EventResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.create(user.getId(), request));
    }

    /** Atualiza um evento do usuário autenticado. */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(user.getId(), id, request));
    }

    /** Remove um evento do usuário autenticado. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id) {
        eventService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
