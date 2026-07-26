package structures.graphs.implementations;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;

import java.util.LinkedHashSet;
import java.util.Set;


 //DFS recursivo con retroceso (backtracking) y se mantiene un Set "path" con 
 //la ruta actual durante la recursividad y si una rama no conduce al destino, el
 //nodo se elimina de "path" antes de regresar, y se prueba la
 //siguiente rama disponible
 
public class DFSPathFinder<T> implements PathFinder<T> {

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {

        Set<T> visitados = new LinkedHashSet<>();
        Set<T> path = new LinkedHashSet<>();
        boolean encontrado = dfs(graph, start, end, visitados, path);
        if (!encontrado) {
            path.clear();
        }
        return new PathResult<>(visitados, path);
    }
    private boolean dfs(Graph<T> graph, T current, T end,Set<T> visitados, Set<T> path) {
        visitados.add(current);
        path.add(current);

        if (current.equals(end)) {
            return true;
        }
        for (Node<T> vecino : graph.getVecinos(current)) {
            if (!visitados.contains(vecino.getValue())) {
                if (dfs(graph, vecino.getValue(), end, visitados, path)) {
                    return true;
                }
            }
        }
        // Backtracking: esta rama no condujo al destino.
        path.remove(current);
        return false;
    }
}
