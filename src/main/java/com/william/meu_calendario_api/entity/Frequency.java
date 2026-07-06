package com.william.meu_calendario_api.entity;

/**
 * Frequências de recorrência suportadas pelo calendário.
 * Os valores em minúsculo espelham o campo frequency usado no frontend
 * (ver calendarState.dayEvents em showCalendar.js).
 */
public enum Frequency {
    ONCE,
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    YEARLY
}
