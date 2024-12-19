package gui.view;

import core.graphics.GraphicChip;
import gui.controller.ScreenViewController;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ScreenView {
	
	private static final int DARK_LIGHT_START_ADDRESS = 0x0000;
	private static final int COLOR_START_ADDRESS = 0x1000;

    private final Canvas canvas;
    private Stage retro24Stage;

    public ScreenView() {
        // Canvas erstellen
        this.canvas = new Canvas(GraphicChip.PIXEL_WIDTH * 10, GraphicChip.PIXEL_HEIGHT * 10); // Skalierung der Pixel
    }
    
    /**
	 * Startet den Bildschirm des Retro24
	 */
    public void showRetro24Screen(ScreenViewController sc) {
    	if (retro24Stage != null) {
    		retro24Stage.close();
    	}
    	
    	retro24Stage = new Stage();
    	
    	// GUI-Layout erstellen
        BorderPane root = new BorderPane();
        
        root.setCenter(canvas);
        
        // Szene und Stage konfigurieren
        Scene scene = new Scene(root, 640, 640); // Größe anpassen
        retro24Stage.setTitle("Retro24");
        retro24Stage.setScene(scene);
        retro24Stage.setResizable(false);
        retro24Stage.show();
        
        retro24Stage.setOnCloseRequest((close) -> sc.afterRun());
        
        // Pause für 3 Sekunden, bevor das System gestartet wird
        // um den Willkommensbildschirm zu zeigen
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> sc.runSystem());
        pause.play();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    // 0=dunkel, 1=hell ab $E000 und einmal
    // 0=monochrom, 1=farbig ab $F000
    public void updateScreen(byte[] memory) {
    	GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        int videoMemPos = 0;
    	for (int i=0; i < GraphicChip.PIXEL_HEIGHT; i ++) {
    		for (int j = 0; j < GraphicChip.PIXEL_WIDTH; j ++, videoMemPos++) {
    			
    			// Berechne die Speicheradressen der Werte für Helligkeit und Farbe für den aktuellen Pixel:
    			int darkLightIndex = DARK_LIGHT_START_ADDRESS + videoMemPos;
    			int colorIndex = COLOR_START_ADDRESS + videoMemPos;
			
    			boolean brightness = (memory[darkLightIndex] & 0x1) == 1;
    			boolean colorValue = (memory[colorIndex] & 0x1) == 1;
    			
    			// Berechne die Farbe basierend auf den Werten
                Color color = calculateColor(brightness, colorValue);

                // Zeichne den Pixel
                gc.setFill(color);
                gc.fillRect(j * 10, i * 10, 10, 10); // Skalierung: 10x10 pro Pixel
    		}
    	}
    }	
	/**
	 * Berechnet die Farbe basierend auf Helligkeit und Modus.
	 * @param brightness 0 = dunkel, 1 = hell
	 * @param mode 0 = monochrom, 1 = farbig
	 * @return Die entsprechende Farbe.
	 */
	private Color calculateColor (boolean brightness, boolean mode) {
	    if (!mode && !brightness) return Color.BLACK; // Monochrom dunkel
	    if (!mode && brightness) return Color.WHITE;  // Monochrom hell
	    if (mode && !brightness) return Color.BLUE;   // Farbe dunkel (Blau)
	    if (mode && brightness) return Color.YELLOW;  // Farbe hell (Gelb)
	    return Color.BLACK; // Fallback (sollte nie passieren)
	}
	
	public Stage getStage() {
		return this.retro24Stage;
	}
} 





