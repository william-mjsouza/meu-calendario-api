package com.william.meu_calendario_api.dto.event;

import com.william.meu_calendario_api.entity.Frequency;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Resposta com os dados do evento; formato casa com o objeto dayEvent do frontend
 * (nomes em snake_case, datas ISO, horas HH:mm).
 */
public record EventResponse(
        Long id,
        String title,
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate start_date,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_event_date,

        @JsonFormat(pattern = "HH:mm")
        LocalTime start_hour,

        @JsonFormat(pattern = "HH:mm")
        LocalTime end_hour,

        boolean all_day,
        Frequency frequency,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_date,

        String color,
        String formGroupColor,
        boolean concluded,
        Frequency originalFrequency
) {}
