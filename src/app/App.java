package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import controllers.MapController;
import views.MainFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // se usa el look and feel por defecto si falla
            }
            MapController controller = new MapController();
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    } 
}
