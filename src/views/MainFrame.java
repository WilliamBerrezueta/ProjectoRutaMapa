package views;

import controllers.MapController;
import models.MapPoint;
import models.VisualizationMode;
import structures.graphs.PathResult;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

 //Vista principaly contiene el MapPanel y el panel de controles
 //implementa MapController.Listener para reaccionar a los eventos del
 //controlador y traducirlos en dibujo o en mensajes

public class MainFrame extends JFrame implements MapController.Listener {

    private enum EditMode { NONE, AGREGAR_NODO, CONECTAR, ELIMINAR_NODO, ELIMINAR_ARISTA }
    private final MapController controller;
    private final MapPanel mapPanel = new MapPanel();
    private final JComboBox<String> inicioCombo = new JComboBox<>();
    private final JComboBox<String> destinoCombo = new JComboBox<>();
    private final JComboBox<String> algoritmoCombo = new JComboBox<>(new String[]{"BFS", "DFS"});
    private final JComboBox<String> modoCombo = new JComboBox<>(new String[]{"Exploracion completa", "Ruta final"});
    private final JCheckBox bidireccionalCheck = new JCheckBox("Bidireccional", true);
    private final JLabel statusLabel = new JLabel("Listo.");
    private EditMode editMode = EditMode.NONE;
    private String pendingFrom = null;

    public MainFrame(MapController controller) {
        super("Rutas en mapa de calles - BFS y DFS");
        this.controller = controller;
        controller.setListener(this);

        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
        add(buildControlPanel(), BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);

        //se usa una implementación anónima aquí para poder darle nombres a los métodos de reenvío
        mapPanel.setListener(new MapPanel.Listener() {
            @Override
            public void onNodeClicked(String nodeId) {
                handleNodeClicked(nodeId);
            }

            @Override
            public void onEmptySpaceClicked(int modelX, int modelY) {
                handleEmptySpaceClicked(modelX, modelY);
            }
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    private JComponent buildControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(260, 0));
        panel.add(sectionLabel("Mapa"));
        panel.add(buttonFor("Cargar imagen de mapa", e -> cargarImagen()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(sectionLabel("Edicion de nodos y aristas"));
        ButtonGroup modeGroup = new ButtonGroup();
        panel.add(modeToggle("Agregar nodo", EditMode.AGREGAR_NODO, modeGroup));
        panel.add(modeToggle("Conectar nodos", EditMode.CONECTAR, modeGroup));
        panel.add(modeToggle("Eliminar nodo", EditMode.ELIMINAR_NODO, modeGroup));
        panel.add(modeToggle("Eliminar arista", EditMode.ELIMINAR_ARISTA, modeGroup));
        panel.add(bidireccionalCheck);
        panel.add(Box.createVerticalStrut(10));
        panel.add(sectionLabel("Persistencia"));
        panel.add(buttonFor("Guardar configuracion", e -> guardarConfiguracion()));
        panel.add(buttonFor("Cargar configuracion", e -> cargarConfiguracion()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(sectionLabel("Busqueda de ruta"));
        panel.add(labeled("Inicio (A):", inicioCombo));
        panel.add(labeled("Destino (B):", destinoCombo));
        panel.add(labeled("Algoritmo:", algoritmoCombo));
        panel.add(labeled("Modo:", modoCombo));
        panel.add(buttonFor("Ejecutar busqueda", e -> ejecutarBusqueda()));
        panel.add(buttonFor("Limpiar recorrido", e -> mapPanel.clearHighlights()));
        panel.add(buttonFor("Nuevo caso de comparacion", e -> nuevoCaso()));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton buttonFor(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(action);
        return button;
    }

    private JToggleButton modeToggle(String text, EditMode mode, ButtonGroup group) {
        JToggleButton toggle = new JToggleButton(text);
        toggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggle.addActionListener(e -> {
            editMode = toggle.isSelected() ? mode : EditMode.NONE;
            pendingFrom = null;
            setStatus("Modo: " + (toggle.isSelected() ? text : "ninguno"));
        });
        group.add(toggle);
        return toggle;
    }

    private JPanel labeled(String text, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(new JLabel(text), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        return row;
    }

    ///////////////////////////////////////////////////////////////////////
    // ========================== ACCIONES DE UI ======================= //
    ///////////////////////////////////////////////////////////////////////

    private void cargarImagen() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage image = ImageIO.read(chooser.getSelectedFile());
                mapPanel.setBackgroundImage(image);
                setStatus("Imagen de mapa cargada");
            } catch (Exception ex) {
                setStatus("Error al cargar la imagen: " + ex.getMessage());
            }
        }
    }

    private void guardarConfiguracion() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            controller.save(chooser.getSelectedFile().getAbsolutePath());
            setStatus("Configuracion guardada.");
        }
    }

    private void cargarConfiguracion() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            controller.load(chooser.getSelectedFile().getAbsolutePath());
            setStatus("Configuracion cargada.");
        }
    }

    private void ejecutarBusqueda() {
        String inicio = (String) inicioCombo.getSelectedItem();
        String destino = (String) destinoCombo.getSelectedItem();
        String algoritmo = (String) algoritmoCombo.getSelectedItem();
        VisualizationMode modo = modoCombo.getSelectedIndex() == 0
                ? VisualizationMode.EXPLORATION
                : VisualizationMode.FINAL_PATH;

        mapPanel.clearHighlights();
        controller.runSearch(inicio, destino, algoritmo, modo);
    }

    private void nuevoCaso() {
        controller.nuevoCaso();
        setStatus("Caso de comparacion actual: " + controller.getCasoActual());
    }

    private void handleNodeClicked(String nodeId) {
        switch (editMode) {
            case CONECTAR:
                if (pendingFrom == null) {
                    pendingFrom = nodeId;
                    setStatus("Nodo origen: " + nodeId + ", seleccione el destino");
                } else {
                    controller.addEdge(pendingFrom, nodeId, bidireccionalCheck.isSelected());
                    pendingFrom = null;
                }
                break;
            case ELIMINAR_NODO:
                controller.removeNode(nodeId);
                break;
            case ELIMINAR_ARISTA:
                if (pendingFrom == null) {
                    pendingFrom = nodeId;
                    setStatus("Arista desde: " + nodeId + ", seleccione el otro extremo");
                } else {
                    controller.removeEdge(pendingFrom, nodeId);
                    pendingFrom = null;
                }
                break;
            default:
                setStatus("Nodo: " + nodeId);
        }
    }

    private void handleEmptySpaceClicked(int modelX, int modelY) {
        if (editMode == EditMode.AGREGAR_NODO) {
            String id = JOptionPane.showInputDialog(this, "Identificador del nuevo nodo:");
            if (id != null && !id.trim().isEmpty()) {
                controller.addNode(id.trim(), modelX, modelY);
            }
        }
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    ///////////////////////////////////////////////////////////////////////
    // ================= MapController.Listerner ======================= //
    ///////////////////////////////////////////////////////////////////////


    //Cada vez que el controlador avisa que el grafo cambió
    //nodo/arista agregada/eliminada, o una configuración recién cargada, 
    //se hacen dos cosas
    //pedirle al mapPanel que se redibuje con los datos actuales, 
    //y actualizar los combos de "Inicio"/"Destino" 
    @Override
    public void onModelChanged() {
        mapPanel.refresh(controller.getNodes(), controller.getEdges());
        refreshCombos();
    }

    private void refreshCombos() {
        String prevInicio = (String) inicioCombo.getSelectedItem();
        String prevDestino = (String) destinoCombo.getSelectedItem();

        inicioCombo.removeAllItems();        //Antes de vaciar los combos, se guarda qué había seleccionado el usuario
                                             //esto es importante porque, si no se hiciera, 
                                             //cada vez que el usuario agregara un nodo nuevo, 
                                             //los combos de "Inicio"/"Destino" 
                                             //perderían la selección actual y volverían a quedar en blanco
        destinoCombo.removeAllItems();
        for (MapPoint p : controller.getNodes()) {
            inicioCombo.addItem(p.getId());
            destinoCombo.addItem(p.getId());
        }
        if (prevInicio != null) inicioCombo.setSelectedItem(prevInicio);
        if (prevDestino != null) destinoCombo.setSelectedItem(prevDestino);
    }

    //Traduce el evento abstracto del controlador 
    //a la llamada concreta correspondiente sobre mapPanel
    @Override
    public void onAnimationStep(MapPoint point, boolean isPath) {
        if (isPath) {
            mapPanel.markPath(point.getId());     //este es el único lugar de todo el proyecto 
                                                  //donde se decide "este método de dibujo específico"
        } else {
            mapPanel.markVisited(point.getId());
        }
    }

    //arma un mensaje legible con los datos del resultado
    @Override
    public void onAnimationFinished(PathResult<MapPoint> result, long elapsedNanos) {
        double ms = elapsedNanos / 1_000_000.0;
        if (result.hasPath()) {
            setStatus(String.format(
                    "Busqueda completa: %d nodos visitados, ruta de %d nodos, %.3f ms.",
                    result.getVisitados().size(), result.getPath().size(), ms));
        } else {
            setStatus(String.format(
                    "No existe una ruta entre los nodos seleccionados (%d nodos visitados, %.3f ms).",
                    result.getVisitados().size(), ms));
        }
    }

    //Cada error se muestra dos veces, 
    //de forma complementaria 
    //en la barra de estado 
    //y en un cuadro de diálogo
    @Override
    public void onError(String message) {
        setStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.WARNING_MESSAGE);
    }
}

