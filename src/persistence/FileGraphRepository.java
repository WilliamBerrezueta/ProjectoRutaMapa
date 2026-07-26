package persistence;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.EdgeRecord;
import models.MapPoint;

// Persistencia en un archivo de texto con formato JSON:
  //{
    //"nodes": [ { "id": "A", "x": 120, "y": 85 }, ... ],
    //"edges": [ { "from": "A", "to": "B", "bidirectional": true }, ... ]
  //}
 
  //No se usa ninguna libreria externa de JSON
  //en su lugar se usa un lector/escritor propio basado en
  //expresiones regulares, suficiente para este esquema
 
  //Al leer, se valida y descarta a los identificadores repetidos o vacios, y
  //aristas que referencian nodos inexistentes

public class FileGraphRepository implements GraphRepository {

    private static final Pattern NODE_PATTERN = Pattern.compile(
            "\\{\\s*\"id\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"x\"\\s*:\\s*(-?\\d+)\\s*,\\s*\"y\"\\s*:\\s*(-?\\d+)\\s*\\}");

    private static final Pattern EDGE_PATTERN = Pattern.compile(
            "\\{\\s*\"from\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"to\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"bidirectional\"\\s*:\\s*(true|false)\\s*\\}");

    @Override
    public void save(GraphData data, String path) throws IOException {
        StringBuilder sb = new StringBuilder();  //Se usa StringBuilder en vez de 
        sb.append("{\n  \"nodes\": [\n");   // ir concatenando String + String dentro de un bucle
        List<MapPoint> nodes = data.getNodes();
        for (int i = 0; i < nodes.size(); i++) {   //Se usa un for con índice por que se necesita 
            MapPoint p = nodes.get(i);             //saber si el elemento actual es el último de la lista
                                                   //para decidir si hay que poner una coma

            sb.append("    { \"id\": \"").append(escape(p.getId()))//escape(p.getId()) 
              .append("\", \"x\": ").append(p.getX())              //evita que un id con comillas dentro rompa el archivo
              .append(", \"y\": ").append(p.getY()).append(" }");
            if (i < nodes.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n  \"edges\": [\n");
        List<EdgeRecord> edges = data.getEdges();
        for (int i = 0; i < edges.size(); i++) {
            EdgeRecord e = edges.get(i);
            sb.append("    { \"from\": \"").append(escape(e.getFrom()))
              .append("\", \"to\": \"").append(escape(e.getTo()))
              .append("\", \"bidirectional\": ").append(e.isBidirectional()).append(" }");
            if (i < edges.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        try (Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), StandardCharsets.UTF_8))) {//garantiza que el archivo se cierre correctamente
            w.write(sb.toString());
        }
    }
    @Override
    public GraphData load(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        //Lee todo el archivo de una sola vez como un solo String

        String nodesBlock = extractArrayBlock(content, "\"nodes\"");//primero se recorta el String en dos pedazos
        String edgesBlock = extractArrayBlock(content, "\"edges\"");//Asi no hay riesgo de que algo que "se parezca" 
                                                                        //a un nodo dentro del bloque de edges 
                                                                        //sea interpretado por error como un nodo real

        List<MapPoint> nodes = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        if (nodesBlock != null) {
            Matcher m = NODE_PATTERN.matcher(nodesBlock);
            while (m.find()) {
                String id = m.group(1);
                if (id == null || id.trim().isEmpty() || seenIds.contains(id)) {
                    continue; // datos incompletos o repetidos, se descartan
                }
                int x = Integer.parseInt(m.group(2));
                int y = Integer.parseInt(m.group(3));
                nodes.add(new MapPoint(id, x, y));
                seenIds.add(id);
            }
        }
        List<EdgeRecord> edges = new ArrayList<>();
        if (edgesBlock != null) {
            Matcher m = EDGE_PATTERN.matcher(edgesBlock);
            while (m.find()) {
                String from = m.group(1);
                String to = m.group(2);
                boolean bidi = Boolean.parseBoolean(m.group(3));
                if (from == null || to == null
                        || !seenIds.contains(from) || !seenIds.contains(to)) {
                    continue; //como seenIds ya contiene únicamente 
                             //los id de nodos que sí pasaron la validación anterior,
                            //esta línea descarta cualquier arista que haga 
                            // referencia a un nodo que no exista
                }
                edges.add(new EdgeRecord(from, to, bidi));
            }
        }
        return new GraphData(nodes, edges);
    }

    //Extrae el bloque "[ ... ]" que sigue a una clave dada, respetando anidamiento
    private String extractArrayBlock(String content, String key) {
        int keyIdx = content.indexOf(key);
        if (keyIdx < 0) return null;
        int start = content.indexOf('[', keyIdx);
        if (start < 0) return null;
        int depth = 0;
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return content.substring(start, i + 1);
            }
        }
        return null;
    }
    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
        //Esta función reemplaza cada \ por \\ y cada " por \", 
        //que es la forma estándar de "escapar" esos caracteres especiales 
        //antes de guardarlos dentro de un archivo

        //Si un id de nodo contuviera, por ejemplo, una comilla ("), 
        //al escribirlo directamente dentro de "id": "..." rompería el formato
    }
    
}
