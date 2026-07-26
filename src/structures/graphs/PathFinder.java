package structures.graphs;

public interface PathFinder<T> {

    //La vista y el controlador dependen unicamente de esta interfaz

    PathResult<T> find(Graph<T> graph, T start, T end);
    
}
