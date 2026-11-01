package com.med.scheduler.graph.core;

/**
 * Função condicional que avalia o estado atual e determina o identificador do próximo nó.
 *
 * @param <T> Tipo do estado.
 */
@FunctionalInterface
public interface EdgeCondition<T> {

    String next(T state);
}
