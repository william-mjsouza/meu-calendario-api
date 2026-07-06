package com.william.meu_calendario_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento do calendário. Espelha o objeto dayEvent do frontend
 * (título, descrição, cor, datas, horários, frequência e término da recorrência).
 * Cada evento pertence a exatamente um usuário.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Data de início do evento. */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Data em que o evento termina (pode ser >= startDate). */
    @Column(name = "end_event_date")
    private LocalDate endEventDate;

    /** Hora de início (00:00–23:59). */
    @Column(name = "start_hour")
    private LocalTime startHour;

    /** Hora de fim (00:00–23:59). */
    @Column(name = "end_hour")
    private LocalTime endHour;

    /** Flag "O dia todo" (00:00–23:59). */
    @Column(name = "all_day", nullable = false)
    private boolean allDay;

    /** Frequência de recorrência. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    /** Data em que a recorrência termina (null = "Nunca"). */
    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    /** Cor do marcador (RGB serializado, ex.: "rgb(242, 226, 5)"). */
    @Column(nullable = false, length = 50)
    private String color;

    /** Cor da faixa do topo do formulário (usada apenas no cliente para restaurar visual). */
    @Column(name = "form_group_color", length = 50)
    private String formGroupColor;

    /** Marcado como concluído? */
    @Column(nullable = false)
    private boolean concluded;

    /** Frequência original (preservada quando o evento é marcado como concluído e forçado para ONCE). */
    @Enumerated(EnumType.STRING)
    @Column(name = "original_frequency")
    private Frequency originalFrequency;

    /** Dono do evento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
