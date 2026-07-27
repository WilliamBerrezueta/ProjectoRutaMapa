package views;

import models.EdgeRecord;
import models.MapPoint;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

 //Vista que solo dibuja lo que el controlador le indica
 
public class MapPanel extends JPanel {

     //Eventos de interaccion del usuario sobre el mapa, MapPanel solo hace
     //hit-testing sobre los nodos ya dibujados y reporta clics
     //quien implemente esta interfaz es MainFrame y decide que significa cada
     //clic segun el modo de edicion activo.
     
    public interface Listener {
        //click sobre un nodo existente
        void onNodeClicked(String nodeId);

        //click en un punto vacio, en coordenadas del modelo
        void onEmptySpaceClicked(int modelX, int modelY);
    }

    private static final int NODE_RADIUS = 10;
    private static final int LOGICAL_WIDTH = 800;
    private static final int LOGICAL_HEIGHT = 600;

    private BufferedImage backgroundImage;
    private Collection<MapPoint> nodes = new ArrayList<>();//nodes y edges son lo que el controlador le pasa para dibujar
    private List<EdgeRecord> edges = new ArrayList<>();
    private final Set<String> visitedHighlight = new LinkedHashSet<>();//visitedHighlight y pathHighlight son dos conjuntos 
                                                                       //de id que se van acumulando mientras corre la animación
    private final Set<String> pathHighlight = new LinkedHashSet<>();
    private Listener listener;

    public MapPanel() {
        setBackground(Color.WHITE);
        setDoubleBuffered(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setBackgroundImage(BufferedImage image) {
        this.backgroundImage = image;
        repaint();
    }

    public void refresh(Collection<MapPoint> nodes, List<EdgeRecord> edges) {
        this.nodes = nodes;
        this.edges = edges;                    //son todos los métodos públicos 
                                               //que MainFrame puede usar 
                                               //para controlar qué se ve
        repaint();
    }

    public void markVisited(String nodeId) {
        visitedHighlight.add(nodeId);
        repaint();
    }

    public void markPath(String nodeId) {
        pathHighlight.add(nodeId);
        repaint();
    }

    public void clearHighlights() {
        visitedHighlight.clear();
        pathHighlight.clear();
        repaint();
    }

    ///////////////////////////////////////////////////////////////////////
    // ============================= DIBUJADO ========================== //
    ///////////////////////////////////////////////////////////////////////

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Transform t = computeTransform();//método que Swing llama automáticamente cada vez que el panel necesita redibujarse

        drawBackground(g2, t);
        drawEdges(g2, t);
        drawNodes(g2, t);
    }

    private void drawBackground(Graphics2D g2, Transform t) {      //Si hay imagen cargada, se dibuja escalada
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, t.offsetX, t.offsetY,
                    (int) (backgroundImage.getWidth() * t.scale),
                    (int) (backgroundImage.getHeight() * t.scale), null);
        } else {
            g2.setColor(new Color(245, 245, 245));
            g2.fillRect(t.offsetX, t.offsetY,
                    (int) (LOGICAL_WIDTH * t.scale), (int) (LOGICAL_HEIGHT * t.scale)); //Si no hay imagen, se dibuja un rectángulo gris claro con borde
            g2.setColor(new Color(225, 225, 225));
            g2.drawRect(t.offsetX, t.offsetY,
                    (int) (LOGICAL_WIDTH * t.scale), (int) (LOGICAL_HEIGHT * t.scale));
        }
    }

    private void drawEdges(Graphics2D g2, Transform t) {
        g2.setStroke(new BasicStroke(2f));  //Por cada arista, se busca el MapPoint real de sus dos extremos
        for (EdgeRecord edge : edges) {
            MapPoint from = findNode(edge.getFrom());
            MapPoint to = findNode(edge.getTo());
            if (from == null || to == null) continue;//El if es como una protección
                                                     //por si alguna arista queda huerfana

            int x1 = t.toScreenX(from.getX());
            int y1 = t.toScreenY(from.getY()); //Las coordenadas del modelo 
                                               //se convierten a coordenadas 
                                               //de pantalla con t.toScreenX/toScreenY 
                                               // antes de dibujar la línea
            int x2 = t.toScreenX(to.getX());
            int y2 = t.toScreenY(to.getY());

            g2.setColor(Color.BLACK);
            g2.drawLine(x1, y1, x2, y2);

            if (!edge.isBidirectional()) {
                drawArrowHead(g2, x1, y1, x2, y2);
            }
        }
    }

    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowLen = 10;
        double a1 = angle + Math.toRadians(150);
        double a2 = angle - Math.toRadians(150);

        int ax1 = x2 + (int) (arrowLen * Math.cos(a1));
        int ay1 = y2 + (int) (arrowLen * Math.sin(a1));  //La punta de flecha
        int ax2 = x2 + (int) (arrowLen * Math.cos(a2));
        int ay2 = y2 + (int) (arrowLen * Math.sin(a2));

        g2.drawLine(x2, y2, ax1, ay1);
        g2.drawLine(x2, y2, ax2, ay2);
    }

    private void drawNodes(Graphics2D g2, Transform t) {
        for (MapPoint p : nodes) {
            int x = t.toScreenX(p.getX());
            int y = t.toScreenY(p.getY());

            Color fill;
            if (pathHighlight.contains(p.getId())) {
                fill = new Color(46, 139, 87);   // ruta final: verde
            } else if (visitedHighlight.contains(p.getId())) {
                fill = new Color(70, 130, 180);  // explorado: azul
            } else {
                fill = new Color(200, 60, 60);   // nodo normal: rojo
            }

            g2.setColor(fill);
            g2.fill(new Ellipse2D.Double(x - NODE_RADIUS, y - NODE_RADIUS,  //se resta el radio a x e y 
                                                                            //así el círculo queda centrado 
                                                                            //exactamente en el punto (x, y)
                    NODE_RADIUS * 2, NODE_RADIUS * 2));

            g2.setColor(Color.BLACK);
            g2.draw(new Ellipse2D.Double(x - NODE_RADIUS, y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2));
            g2.drawString(p.getId(), x + NODE_RADIUS + 2, y - NODE_RADIUS);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // ========================== INTERACCION ========================== //
    ///////////////////////////////////////////////////////////////////////

    private void handleClick(int screenX, int screenY) {  //decide si un clic cayó sobre un nodo o no
        if (listener == null) return;
        Transform t = computeTransform();
        for (MapPoint p : nodes) {
            int x = t.toScreenX(p.getX());
            int y = t.toScreenY(p.getY());
            double dist = Math.hypot(screenX - x, screenY - y);//se mide la distancia entre el clic y ese punto con Math.hypot(dx, dy)
            if (dist <= NODE_RADIUS + 4) {
                listener.onNodeClicked(p.getId());
                return;
            }
        }
        int modelX = t.toModelX(screenX);
        int modelY = t.toModelY(screenY);
        listener.onEmptySpaceClicked(modelX, modelY);
    }

    private MapPoint findNode(String id) {  //Búsqueda lineal simple de un nodo por id dentro de la colección actual
        for (MapPoint p : nodes) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    //mantener proporciones adecuadas al redimensionar la ventana
    private Transform computeTransform() {
        int logicalW = backgroundImage != null ? backgroundImage.getWidth() : LOGICAL_WIDTH;
        int logicalH = backgroundImage != null ? backgroundImage.getHeight() : LOGICAL_HEIGHT;
        int panelW = Math.max(getWidth(), 1);
        int panelH = Math.max(getHeight(), 1);
        double scale = Math.min((double) panelW / logicalW, (double) panelH / logicalH);
        int offsetX = (int) ((panelW - logicalW * scale) / 2);
        int offsetY = (int) ((panelH - logicalH * scale) / 2);

        return new Transform(scale, offsetX, offsetY);
    }

    //clase privada y estática, anidada dentro de MapPanel
    //ofrece cuatro métodos de conversión, dos parejas exactamente inversas entre sí
    private static class Transform {
        final double scale;
        final int offsetX;
        final int offsetY;
        Transform(double scale, int offsetX, int offsetY) {
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        int toScreenX(int modelX) {
            return offsetX + (int) (modelX * scale);
        }

        int toScreenY(int modelY) {
            return offsetY + (int) (modelY * scale);
        }

        int toModelX(int screenX) {
            return (int) ((screenX - offsetX) / scale);
        }

        int toModelY(int screenY) {
            return (int) ((screenY - offsetY) / scale);
        }
    }
}