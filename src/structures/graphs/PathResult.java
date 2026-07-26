package structures.graphs;

import java.util.Set;

public class PathResult<T> {

    private final Set<T> visitados;
    private final Set<T> path;

    public PathResult(Set<T> visitados, Set<T> path) {
        this.visitados = visitados;
        this.path = path;
    }

    public Set<T> getVisitados() {
        return visitados;
    }

    public Set<T> getPath() {
        return path;
    }

    //true si se encuentra una ruta (path no vacio) y se usa en el controlador
    //y en la vista para distinguir "hay ruta" de "no hay ruta" sin repetir
    //la comprobacion en varios lugares.
    public boolean hasPath() {
        return path != null && !path.isEmpty();
    }

    @Override
    public String toString() {
        if (path.isEmpty()) {
            return "No se encontró un camino entre los nodos.";
        } else {
            return "visitados=" + visitados + "\n" +
                   "path=" + path;
        }
    }
    
}
