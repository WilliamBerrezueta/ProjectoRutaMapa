package structures.graphs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import structures.node.Node;

public class Graph<T> {

    Map<Node<T>, Set<Node<T>>> nodes;

    public Graph(){
        this.nodes = new HashMap<>();
    }

    public void add(T value){
        Node<T> node = new Node<>(value);
        nodes.putIfAbsent(node, new HashSet<>());

    }

    // agregar una conexion bidireccional entre dos nodos
    public void addEdge(T from, T to) {
        add(from);
        add(to);

        Node<T> fromNode = new Node<>(from);
        Node<T> toNode = new Node<>(to);

        nodes.get(fromNode).add(toNode);
        nodes.get(toNode).add(fromNode);
    }

    public void addEdgeUni(T from, T to) {
        add(from);
        add(to);

        Node<T> fromNode = new Node<>(from);
        Node<T> toNode = new Node<>(to);

        nodes.get(fromNode).add(toNode);
    }

    public void print(){
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
            for(Node<T> node : entry.getValue()){
                System.out.println("    " + node);
            }
            System.out.println();
        }
    }

    // Total de direcciones
    public int size(){
        return nodes.size();
    }
    // total de conecciones
    public int edges(){
        int totalEdges = 0;
        for (Set<Node<T>> edges : nodes.values()) {
            totalEdges += edges.size();
        }
        return totalEdges / 2; // Dividimos entre 2 porque cada arista se cuenta dos veces (una para cada nodo)
    }

    // para eliminar conexiones entre nodos
    public void removeEdge(T from, T to){
        Node<T> fromNode = new Node<>(from);
        Node<T> toNode = new Node<>(to);
        nodes.get(fromNode).remove(toNode);
        nodes.get(toNode).remove(fromNode);
    }

    // para cambiar direccion de bidireccional a unidireccional
    public void removeEdgeUni(T from, T to){
        Node<T> fromNode = new Node<>(from);
        Node<T> toNode = new Node<>(to);
        nodes.get(fromNode).remove(toNode);
    }
    // para eliminar un nodo y todas sus conexiones
    public void removeNode(T value){
        Node<T> nodeToRemove = new Node<>(value);
        nodes.remove(nodeToRemove);
        for (Set<Node<T>> edges : nodes.values()) {
            edges.remove(nodeToRemove);
        }
    }

    public Set<Node<T>> getVecinos(T currente) {

        Node<T> node = new Node<>(currente);
        return nodes.getOrDefault(node, new HashSet<>());
    }
    
}
