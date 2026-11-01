package com.med.scheduler.domain.model;

import java.util.Objects;

/**
 * Entidade de domínio representando um Médico.
 */
public record Doctor(
        String id,
        String name,
        String specialty
) {
    public Doctor {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(specialty, "specialty cannot be null");
    }
}
