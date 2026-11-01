package com.med.scheduler.graph.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Engine do Grafo de Estados (DAG / StateGraph) determinístico.
 *
 * @param <T> Tipo do estado imutável.
 */
public class StateGraph<T> {

    public static final String END = "__END__";

    private final String entryNode;
    private final Map<String, Node<T>> nodes = new HashMap<>();
    private final Map<String, String> edges = new HashMap<>();
    private final Map<String, EdgeCondition<T>> conditionalEdges = new HashMap<>();

    public StateGraph(String entryNode) {
        this.entryNode = Objects.requireNonNull(entryNode, "entryNode cannot be null");
    }

    public StateGraph<T> addNode(String id, Node<T> node) {
        Objects.requireNonNull(id, "node id cannot be null");
        Objects.requireNonNull(node, "node cannot be null");
        nodes.put(id, node);
        return this;
    }

    public StateGraph<T> addEdge(String from, String to) {
        Objects.requireNonNull(from, "from node cannot be null");
        Objects.requireNonNull(to, "to node cannot be null");
        edges.put(from, to);
        return this;
    }

    public StateGraph<T> addConditionalEdge(String from, EdgeCondition<T> condition) {
        Objects.requireNonNull(from, "from node cannot be null");
        Objects.requireNonNull(condition, "condition cannot be null");
        conditionalEdges.put(from, condition);
        return this;
    }

    public T execute(T initialState) {
        Objects.requireNonNull(initialState, "initialState cannot be null");

        String currentNodeId = this.entryNode;
        T currentState = initialState;

        while (!END.equals(currentNodeId)) {
            Node<T> node = nodes.get(currentNodeId);
            if (node == null) {
                throw new IllegalStateException("Node with id '" + currentNodeId + "' is not registered in the graph.");
            }

            currentState = node.execute(currentState);

            if (conditionalEdges.containsKey(currentNodeId)) {
                currentNodeId = conditionalEdges.get(currentNodeId).next(currentState);
            } else if (edges.containsKey(currentNodeId)) {
                currentNodeId = edges.get(currentNodeId);
            } else {
                // Sem transição definida -> encerra
                currentNodeId = END;
            }
        }

        return currentState;
    }
}
