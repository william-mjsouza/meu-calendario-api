package com.william.meu_calendario_api.dto.event;

import com.william.meu_calendario_api.entity.Frequency;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Payload usado tanto para criação quanto atualização de eventos.
 * Nomes em snake_case para casar diretamente com o objeto dayEvent do frontend.
 */
public record EventRequest(
        String title,

        String description,

        @NotNull(message = "start_date é obrigatório")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate start_date,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_event_date,

        @JsonFormat(pattern = "HH:mm")
        LocalTime start_hour,

        @JsonFormat(pattern = "HH:mm")
        LocalTime end_hour,

        boolean all_day,

        @NotNull(message = "frequency é obrigatório")
        Frequency frequency,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_date,

        String color,

        String formGroupColor,

        boolean concluded,

        Frequency originalFrequency
) {}
