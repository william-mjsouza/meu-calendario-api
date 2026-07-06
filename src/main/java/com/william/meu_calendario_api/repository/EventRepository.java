package com.william.meu_calendario_api.repository;

import com.william.meu_calendario_api.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    /** Lista todos os eventos do usuário informado. */
    List<Event> findByUserId(Long userId);

    /** Busca um evento específico garantindo que pertence ao usuário. */
    Optional<Event> findByIdAndUserId(Long id, Long userId);
}
