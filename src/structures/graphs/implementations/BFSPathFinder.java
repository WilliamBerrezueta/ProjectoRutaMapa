package structures.graphs.implementations;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;

// BFS por niveles usando una cola y evita volver a encolar el
// mismo nodo dos veces y "parent" guarda el predecesor de cada nodo
// descubierto para poder reconstruir la ruta y "visitados" guarda el orden
// en que los nodos fueron procesados

public class BFSPathFinder<T> implements PathFinder<T> {
    
    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {
        Queue<T> queue = new LinkedList<>();
        Set<T> vecinos = new LinkedHashSet<>();
        Map<Node<T>, Node<T>> parent = new HashMap<>();
        Set<T> visitados = new LinkedHashSet<>();

        queue.add(start);
        vecinos.add(start);
        parent.put(new Node<>(start), null);

        while (!queue.isEmpty()) {

            T current = queue.poll();
            visitados.add(current);

            if (current.equals(end)) {
                return new PathResult<>(visitados, buildPath(parent, end));
            }
            for (Node<T> vecino : graph.getVecinos(current)) {
                if (!vecinos.contains(vecino.getValue())) {

                    vecinos.add(vecino.getValue());
                    parent.put(vecino, new Node<>(current));
                    queue.add(vecino.getValue());
                }
            }
        }
        return new PathResult<>(visitados, new LinkedHashSet<>());
    }
     //Reconstruye la ruta caminando por los predecesores desde el destino
     //hacia el inicio, y la devuelve en orden inicio -> destino (para poder
     //animarla progresivamente sobre el mapa).
     
    private Set<T> buildPath(Map<Node<T>, Node<T>> parent, T end) {
        LinkedList<T> reversed = new LinkedList<>();
        Node<T> at = new Node<>(end);

        while (at != null) {
            reversed.addFirst(at.getValue());
            at = parent.get(at);
        }
        return new LinkedHashSet<>(reversed);
    }
}
