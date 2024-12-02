package gui.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ScreenView {

    private final Canvas canvas;
    private final int width;
    private final int height;
    private final int darkLightStartAddress;
    private final int colorStartAddress;

    public ScreenView(int width, int height, int darkLightStartAddress, int colorStartAddress) {
        this.width = width;
        this.height = height;
        this.darkLightStartAddress = darkLightStartAddress;
        this.colorStartAddress = colorStartAddress;

        // Canvas erstellen
        this.canvas = new Canvas(width * 10, height * 10); // Skalierung der Pixel
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
    	for (int i=0; i < height; i ++) {
    		for (int j = 0; j < width; j ++, videoMemPos++) {
    			
    			// Berechne die Speicheradressen der Werte für Helligkeit und Farbe für den aktuellen Pixel:
    			int darkLightIndex = darkLightStartAddress + videoMemPos;
    			int colorIndex = colorStartAddress + videoMemPos;
			
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
} 





