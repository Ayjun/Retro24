package gui.view;

import core.graphics.GraphicChip;
import gui.controller.ScreenViewController;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
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
        
        scene.setOnKeyPressed(event -> handleKeyPress(event, sc));
        scene.setOnKeyReleased(event -> handleKeyRelease(event, sc));
        
        // Pause für 3 Sekunden, bevor das System gestartet wird
        // um den Willkommensbildschirm zu zeigen
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> sc.runSystem());
        pause.play();
    }

    // 0=dunkel, 1=hell ab $E000 und einmal
    // 0=monochrom, 1=farbig ab $F000
    public void updateScreen(byte[] memory) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        int[][] pixelColors = new int[GraphicChip.PIXEL_HEIGHT][GraphicChip.PIXEL_WIDTH];
        
        int videoMemPos = 0;
        for (int i = 0; i < GraphicChip.PIXEL_HEIGHT; i++) {
            for (int j = 0; j < GraphicChip.PIXEL_WIDTH; j++, videoMemPos++) {
                int darkLightIndex = DARK_LIGHT_START_ADDRESS + videoMemPos;
                int colorIndex = COLOR_START_ADDRESS + videoMemPos;
                
                // Sicherstellen, dass Speicherzugriffe nicht außerhalb der Arraygrenzen liegen
                if (darkLightIndex < memory.length && colorIndex < memory.length) {
                    boolean brightness = (memory[darkLightIndex] & 0x1) == 1;
                    boolean colorValue = (memory[colorIndex] & 0x1) == 1;
                    
                    pixelColors[i][j] = calculateColorValue(brightness, colorValue);
                }
            }
        }
        
        //TODO DoubleBuffering / Offscreen Rendering? 
        // Alles auf einmal zeichnen
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < GraphicChip.PIXEL_HEIGHT; i++) {
            for (int j = 0; j < GraphicChip.PIXEL_WIDTH; j++) {
                gc.setFill(getColorFromValue(pixelColors[i][j]));
                gc.fillRect(j * 10, i * 10, 10, 10);
            }
        }
    }
     
    private int calculateColorValue(boolean brightness, boolean mode) {
        if (!mode && !brightness) return 0; // Monochrom dunkel
        if (!mode && brightness) return 1;  // Monochrom hell
        if (mode && !brightness) return 2;  // Farbe dunkel (Blau)
        if (mode && brightness) return 3;   // Farbe hell (Gelb)
        return 0; // Fallback
    }

    private Color getColorFromValue(int colorValue) {
        switch (colorValue) {
            case 0: return Color.BLACK;
            case 1: return Color.WHITE;
            case 2: return Color.BLUE;
            case 3: return Color.YELLOW;
            default: return Color.BLACK;
        }
    }
    
    private void handleKeyPress(KeyEvent event, ScreenViewController controller) {
        switch (event.getCode()) {
            case UP:
                controller.joystickUpBP().set(true);
                break;
            case DOWN:
                controller.joystickDownBP().set(true);
                break;
            case LEFT:
                controller.joystickLeftBP().set(true);
                break;
            case RIGHT:
                controller.joystickRightBP().set(true);
                break;
            case SPACE:
                controller.joystickFireBP().set(true);
                break;
            default:
                break;
        }
        event.consume();
    }

    private void handleKeyRelease(KeyEvent event, ScreenViewController controller) {
        switch (event.getCode()) {
            case UP:
                controller.joystickUpBP().set(false);
                break;
            case DOWN:
                controller.joystickDownBP().set(false);
                break;
            case LEFT:
                controller.joystickLeftBP().set(false);
                break;
            case RIGHT:
                controller.joystickRightBP().set(false);
                break;
            case SPACE:
                controller.joystickFireBP().set(false);
                break;
            default:
                break;
        }
        event.consume();
    }	
	
	public Stage getStage() {
		return this.retro24Stage;
	}
	

    public Canvas getCanvas() {
        return canvas;
    }
} 





