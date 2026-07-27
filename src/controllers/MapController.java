package controllers;

import models.EdgeRecord;
import models.MapPoint;
import models.VisualizationMode;
import persistence.FileGraphRepository;
import persistence.GraphData;
import persistence.GraphRepository;
import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.graphs.implementations.BFSPathFinder;
import structures.graphs.implementations.DFSPathFinder;

import javax.swing.Timer;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapController {
    
    //Eventos que el controlador envia a la vista
    public interface Listener {
        //El grafo cambio nodo/arista agregada o eliminada
        void onModelChanged();
        //Un paso de animacion, resaltar este punto como visitado o como parte de la ruta
        void onAnimationStep(MapPoint point, boolean isPath);
        //La animacion termino
        void onAnimationFinished(PathResult<MapPoint> result, long elapsedNanos);
        //Un error de validacion o de persistencia para mostrarle al usuario
        void onError(String message);
    }
    private static final String RESULTS_CSV = "results.csv";
    private static final String CSV_HEADER ="Caso,Algoritmo,Inicio,Destino,NodosVisitados,CantidadAristas,TiempoMs";
    private final Map<String, MapPoint> nodesById = new LinkedHashMap<>();
    private final List<EdgeRecord> edges = new ArrayList<>();
    private Graph<MapPoint> graph = new Graph<>();
    private final GraphRepository repository = new FileGraphRepository();
    private Listener listener;
    private Timer animationTimer;
    private int casoActual = 1;

    public void setListener(Listener listener) {
        this.listener = listener;
    }
    ///////////////////////////////////////////////////////////////////////
    // =============================== NODOS =========================== //
    ///////////////////////////////////////////////////////////////////////

    public boolean addNode(String id, int x, int y) {//Se devuelve boolean y no void 
                                                     //para que quien llame a este método sepa
                                                     //si la operación realmente se realizó 
                                                     //o fue rechazada por una validación

        if (id == null || id.trim().isEmpty()) {
            notifyError("El nombre del nodo no puede estar vacío");
            return false;
        }
        if (nodesById.containsKey(id)) {
            notifyError("Ya existe un nodo con el nombre; '" + id + "'");
            return false;
        }
        MapPoint point = new MapPoint(id, x, y);
        nodesById.put(id, point);
        graph.add(point);
        notifyModelChanged();
        return true;
    }

    public boolean removeNode(String id) { //devuelve el MapPoint que se quitó
                                           //o null si ese id no existía 
        MapPoint point = nodesById.remove(id);
        if (point == null) {
            notifyError("El nodo '" + id + "' no existe.");
            return false;
        }
        edges.removeIf(e -> e.getFrom().equals(id) || e.getTo().equals(id));
        graph.removeNode(point);
        notifyModelChanged();
        return true;
    }

    ///////////////////////////////////////////////////////////////////////
    // ============================= ARISTAS =========================== //
    ///////////////////////////////////////////////////////////////////////

    public boolean addEdge(String fromId, String toId, boolean bidirectional) {
        //3 validaciones
        if (fromId.equals(toId)) {
            notifyError("Un nodo no puede conectarse a el mismo");
            return false;
        }
        MapPoint from = nodesById.get(fromId);
        MapPoint to = nodesById.get(toId);
        if (from == null || to == null) {
            notifyError("Los dos nodos deben existir para crear una conexión");
            return false;
        }
        boolean duplicada = edges.stream().anyMatch(e ->
                (e.getFrom().equals(fromId) && e.getTo().equals(toId))
                        || (e.isBidirectional() && e.getFrom().equals(toId) && e.getTo().equals(fromId)));
        if (duplicada) {
            notifyError("La conexión entre '" + fromId + "' y '" + toId + "' ya existe");
            return false;
        }
        edges.add(new EdgeRecord(fromId, toId, bidirectional));
        if (bidirectional) {
            graph.addEdge(from, to);
        } else {
            graph.addEdgeUni(from, to);
        }
        notifyModelChanged();
        return true;
    }
    public boolean removeEdge(String fromId, String toId) {
        boolean removed = edges.removeIf(e ->
                (e.getFrom().equals(fromId) && e.getTo().equals(toId))
                        || (e.isBidirectional() && e.getFrom().equals(toId) && e.getTo().equals(fromId)));
        if (!removed) {
            notifyError("No se encontró una conexión entre '" + fromId + "' y '" + toId + "'.");
            return false;
        }
        MapPoint from = nodesById.get(fromId);  //Aquí removeIf devuelve un boolean, true 
                                                //si al menos un elemento fue eliminado,
                                                //false si ninguno coincidía con la condición 
                                                //se usa ese valor para saber 
                                                //si había algo que borrar
        MapPoint to = nodesById.get(toId);
        if (from != null && to != null) {
            graph.removeEdge(from, to);
        }
        notifyModelChanged();
        return true;
    }

    ///////////////////////////////////////////////////////////////////////
    // ======================== PERSISTENCIA =========================== //
    ///////////////////////////////////////////////////////////////////////

    public void save(String path) {
        try {
            GraphData data = new GraphData(new ArrayList<>(nodesById.values()), new ArrayList<>(edges));
            repository.save(data, path);            //se crea una copia de la colección de valores del mapa, en vez de pasarla directamente
        } catch (Exception ex) {
            notifyError("Error al guardar la configuración: " + ex.getMessage());
        }  //Esto evita que, si FileGraphRepository guardara 
           //esa referencia en algún lado por error, 
           //quedara "conectada en vivo" al estado interno del controlador
    }

    public void load(String path) {
        try {
            GraphData data = repository.load(path);//se lee el archivo completo con repository.load(path)
            nodesById.clear();
            edges.clear();
            graph = new Graph<>(); // Graph<T> no tiene clear(), asi que se reemplaza por uno vacio
            for (MapPoint p : data.getNodes()) {
                nodesById.put(p.getId(), p);
                graph.add(p);          //Primero va el for de nodos, 
                                       //y después el for de aristas, 
                                       //es un orden obligatorio porque
                                       //no se puede crear una arista A -> B 
                                       //si A o B todavía no existen como nodos en nodesById/graph
            }
            for (EdgeRecord e : data.getEdges()) {
                MapPoint from = nodesById.get(e.getFrom());
                MapPoint to = nodesById.get(e.getTo());
                if (from == null || to == null) continue; //Segunda capa de seguridad
                edges.add(e);
                if (e.isBidirectional()) {
                    graph.addEdge(from, to);
                } else {
                    graph.addEdgeUni(from, to);
                }
            }
            notifyModelChanged();
        } catch (Exception ex) {
            notifyError("Error al cargar la configuración: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // ============================ BUSQUEDA =========================== //
    ///////////////////////////////////////////////////////////////////////

    public void runSearch(String startId, String endId, String algoritmo, VisualizationMode mode) {
        //Este es el metodo mas importante del controlador
        if (startId == null || endId == null) {
            notifyError("Debe seleccionar un nodo de inicio y un nodo de destino.");
            return;
        }
        MapPoint start = nodesById.get(startId);
        MapPoint end = nodesById.get(endId);
        if (start == null || end == null) {
            notifyError("El nodo de inicio o de destino no existe.");
            return;
        }

        PathFinder<MapPoint> finder = "BFS".equalsIgnoreCase(algoritmo)//aquí es donde se decide cuál algoritmo usar, 
                                                                       //gracias a que ambos implementan 
                                                                       // la misma interfaz PathFinder
                ? new BFSPathFinder<>()
                : new DFSPathFinder<>();

        long t0 = System.nanoTime();
        PathResult<MapPoint> result = finder.find(graph, start, end);// (finder.find(...)) es exactamente el mismo código sin importar cuál se eligió
        long elapsed = System.nanoTime() - t0;

        registrarResultadoCsv(algoritmo, startId, endId, result, elapsed);
        animate(result, mode, elapsed);   //se registra el resultado en el CSV y se dispara la animación
    }
    private void registrarResultadoCsv(String algoritmo, String startId, String endId,
                                        PathResult<MapPoint> result, long elapsedNanos) {
        int aristasRuta = result.hasPath() ? result.getPath().size() - 1 : 0;//si la ruta tiene, por ejemplo, 
                                                                             //4 nodos (A, B, C, D), 
                                                                             //la cantidad de aristas recorridas 
                                                                             // es 3 (A-B, B-C, C-D)
                                                                             //siempre un nodo menos que la cantidad de arista
        double ms = elapsedNanos / 1_000_000.0;
        String linea = casoActual + "," + algoritmo + "," + startId + "," + endId + ","
                + result.getVisitados().size() + "," + aristasRuta + ","
                + String.format("%.3f", ms);

        try {
            boolean existe = Files.exists(Paths.get(RESULTS_CSV));
            try (Writer w = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(RESULTS_CSV, true), StandardCharsets.UTF_8))) {
                if (!existe) {
                    w.write(CSV_HEADER);
                    w.write("\n");
                }
                w.write(linea);
                w.write("\n");
            }
        } catch (IOException ex) {
            notifyError("No se pudo actualizar results.csv: " + ex.getMessage());
        }
    }

    public void nuevoCaso() {
        casoActual++;
    }                      //avanza el número de "caso" que se está probando

    public int getCasoActual() {
        return casoActual;
    }
    private void animate(PathResult<MapPoint> result, VisualizationMode mode, long elapsedNanos) {
        if (animationTimer != null && animationTimer.isRunning()) {//Si ya había una animación en curso
                                                                   //se detiene la anterior antes de empezar una nueva
            animationTimer.stop();
        }

        List<Object[]> eventos = new ArrayList<>();
        if (mode == VisualizationMode.EXPLORATION) {//En modo EXPLORATION, 
                                                    //primero se agregan todos los nodos 
                                                    //de result.getVisited() 
                                                    //como eventos "no ruta"
            for (MapPoint p : result.getVisitados()) {
                eventos.add(new Object[]{p, Boolean.FALSE});//En modo FINAL_PATH, 
                                                            //como el primer if no se ejecuta, 
                                                            //la lista de eventos queda 
                                                            //compuesta únicamente 
                                                            //por los nodos de la ruta
            }
        }
        for (MapPoint p : result.getPath()) {
            eventos.add(new Object[]{p, Boolean.TRUE});
        }   //en ambos modos se agregan al final los nodos de result.getPath() como eventos "sí ruta"

        Iterator<Object[]> it = eventos.iterator();//en vez de usar un índice (int i), 
                                                   //se usa un iterador porque el temporizador 
                                                   //va a llamar a este código muchas veces

        animationTimer = new Timer(250, null);//crea un temporizador de Swing que "dispara" un evento cada 250 milisegundos
        animationTimer.addActionListener(e -> {
            if (it.hasNext()) {
                Object[] evento = it.next();
                if (listener != null) {
                    listener.onAnimationStep((MapPoint) evento[0], (Boolean) evento[1]);
                }
            } else {
                animationTimer.stop();
                if (listener != null) {
                    listener.onAnimationFinished(result, elapsedNanos);//se avisa onAnimationFinished con el resultado completo, 
                                                                       //para que la vista muestre el resumen final
                }
            }
        });
        animationTimer.start();
    }

    ///////////////////////////////////////////////////////////////////////
    // ============================ CONSULTAS ========================== //
    ///////////////////////////////////////////////////////////////////////

    public Collection<MapPoint> getNodes() {
        return nodesById.values();         //son métodos de consulta 
                                           //para que la vista pueda 
                                           //leer el estado actual cuando lo necesite
    }

    public List<EdgeRecord> getEdges() {
        return edges;
    }

    private void notifyModelChanged() {
        if (listener != null) listener.onModelChanged();//Los dos últimos son ayudantes privados
                                                        //se centralizan en un solo lugar 
                                                        //el if (listener != null) 
                                                        //antes de llamar al listener
                                                        //si en algún momento setListener 
                                                        //todavía no se ha llamado
                                                        //el controlador nunca lanza un NullPointerException
    }

    private void notifyError(String message) {
        if (listener != null) listener.onError(message);
    }
}
