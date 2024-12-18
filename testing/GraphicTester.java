import core.graphics.GraphicChip;
import gui.controller.ScreenViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GraphicTester {
	/**
	 * Testmethode für die Kommunikation zwischen model und view,
	 * lädt immer wechselnd Test- und Welcomescreen in den Grafikspeicher was dann im View angezeigt wird.
	 */

	public static  void test(ScreenViewController screenViewController) {
		GraphicChip graphicChip = screenViewController.getRetro24().getGraphicChip();
		
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			int cycle = 2;
			graphicChip.setUpdateFlag(true);
	        if (cycle % 2 == 0) {
	        	graphicChip.loadRetro24WelcomeScreen();
	        	cycle = 1;
	        } else {
	            graphicChip.loadRandomTestImage();
	            cycle = 2;
	        }
	        screenViewController.updateView();
	    }));
	    timeline.setCycleCount(5); // Wiederholt sich unendlich
	    timeline.play();
	}
}
