package com.william.meu_calendario_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Usuário do sistema. Cada usuário possui seu próprio conjunto de eventos.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do usuário (opcional para exibição). */
    @Column(nullable = false)
    private String name;

    /** E-mail usado como login (único). */
    @Column(nullable = false, unique = true)
    private String email;

    /** Senha em hash BCrypt (nunca armazenada em texto puro). */
    @Column(nullable = false)
    private String password;

    /** Eventos criados por este usuário. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Event> events = new HashSet<>();
}
