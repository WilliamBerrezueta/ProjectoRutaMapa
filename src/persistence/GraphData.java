package persistence;

import java.util.List;

import models.EdgeRecord;
import models.MapPoint;

//Su proposito es actuar como un contenedor simple 
//que agrupa "todos los nodos" y "todas las aristas" 
//en un solo objeto, para poder pasarlo de una sola vez 
//entre el controlador y la capa de persistencia

public class GraphData {

    private final List<MapPoint> nodes;
    private final List<EdgeRecord> edges;

    public GraphData(List<MapPoint> nodes, List<EdgeRecord> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<MapPoint> getNodes() {
        return nodes;
    }

    public List<EdgeRecord> getEdges() {
        return edges;
    }
    
}

//Es literalmente "una caja con dos listas adentro" 
//Se usa List y no Set porque aquí sí importa 
//poder tener duplicados temporales durante la carga 
//osea antes de validarlos y porque una lista 
//es más simple de recorrer en orden al escribir el archivo