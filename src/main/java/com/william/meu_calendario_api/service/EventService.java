package com.william.meu_calendario_api.service;

import com.william.meu_calendario_api.dto.event.EventRequest;
import com.william.meu_calendario_api.dto.event.EventResponse;
import com.william.meu_calendario_api.entity.Event;
import com.william.meu_calendario_api.entity.User;
import com.william.meu_calendario_api.exception.ResourceNotFoundException;
import com.william.meu_calendario_api.repository.EventRepository;
import com.william.meu_calendario_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /** Retorna todos os eventos do usuário autenticado. */
    public List<EventResponse> listByUser(Long userId) {
        return eventRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EventResponse create(Long userId, EventRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Event event = Event.builder().user(user).build();
        applyRequest(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long userId, Long eventId, EventRequest request) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
        applyRequest(event, request);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public void delete(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
        eventRepository.delete(event);
    }

    // ---- Helpers ----

    private void applyRequest(Event event, EventRequest req) {
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setStartDate(req.start_date());
        event.setEndEventDate(req.end_event_date());
        event.setStartHour(req.start_hour());
        event.setEndHour(req.end_hour());
        event.setAllDay(req.all_day());
        event.setFrequency(req.frequency());
        event.setRecurrenceEndDate(req.end_date());
        event.setColor(req.color());
        event.setFormGroupColor(req.formGroupColor());
        event.setConcluded(req.concluded());
        event.setOriginalFrequency(req.originalFrequency());
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStartDate(),
                e.getEndEventDate(),
                e.getStartHour(),
                e.getEndHour(),
                e.isAllDay(),
                e.getFrequency(),
                e.getRecurrenceEndDate(),
                e.getColor(),
                e.getFormGroupColor(),
                e.isConcluded(),
                e.getOriginalFrequency()
        );
    }
}
