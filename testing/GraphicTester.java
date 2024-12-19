import java.util.Random;

import core.Retro24;
import core.graphics.GraphicChip;
import core.graphics.StartScreenLoader;
import gui.controller.ScreenViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GraphicTester {
	private final Retro24 retro24;
	private final ScreenViewController screenViewController;
	private final GraphicChip graphicChip;
	
	public GraphicTester(Retro24 retro24, ScreenViewController screenViewController) {
		this.retro24 = retro24;
		this.graphicChip = retro24.getGraphicChip();
		this.screenViewController = screenViewController;
	}
	
	/**
	 * Testmethode für die Kommunikation zwischen model und view,
	 * lädt immer wechselnd Test- und Welcomescreen in den Grafikspeicher was dann im View angezeigt wird.
	 */
	public void test() {
		
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			int cycle = 2;
			graphicChip.setUpdateFlag(true);
	        if (cycle % 2 == 0) {
	        	StartScreenLoader startScreenLoader = new StartScreenLoader(graphicChip);
	        	startScreenLoader.loadRetro24WelcomeScreen();
	        	cycle = 1;
	        } else {
	            loadRandomTestImage();
	            cycle = 2;
	        }
	        screenViewController.updateView();
	    }));
	    timeline.setCycleCount(5); // Wiederholt sich unendlich
	    timeline.play();
	}
	
	/**
	 * Schreibt ein zufälliges Testbild in den Videospeicher
	 */
	public void loadRandomTestImage() {
		Random r = new Random();
		for (int i = GraphicChip.VIDMEM_START; i < GraphicChip.VIDMEM_END; i++) {
			retro24.writeMemory(i, (byte) r.nextInt());
		}
	}
}
