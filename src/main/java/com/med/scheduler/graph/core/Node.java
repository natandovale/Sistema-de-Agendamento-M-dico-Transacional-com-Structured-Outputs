package com.med.scheduler.graph.core;

/**
 * Representa um nó executável em um StateGraph.
 * Cada nó recebe o estado imutável e retorna um novo estado derivado.
 *
 * @param <T> Tipo do estado compartilhado pelo grafo.
 */
@FunctionalInterface
public interface Node<T> {

    T execute(T state);
}
