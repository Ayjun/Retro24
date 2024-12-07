package gui.view;

import java.io.IOException;

import gui.controller.ControlPanelController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ControlPanelView extends Application{
	
	Stage primaryStage;
	 @Override
	    public void start(Stage primaryStage) {
		 	this.primaryStage = primaryStage;
		 
	        // FXML-Datei laden
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/Retro24ControlPanel.fxml"));
	        
	        Parent root = null;
	        

	        
			try {
				root = loader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        // diese View an den Controller übergeben:
			ControlPanelController controlPanelController = loader.getController();
	        controlPanelController.setControlPanelView(this);
	        
			// Szene erstellen
	        Scene scene = new Scene(root);
	       

	        // Stage konfigurieren
	        primaryStage.setTitle("Retro24 Control Panel");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	    }
	 
	 	public void showError(String title, String message) {
	 		Alert alert = new Alert(AlertType.ERROR);
	 		alert.setTitle(title);
	 		alert.setContentText(message);
	 		alert.show();
	 	}
	 	

	    public static void main(String[] args) {
	        launch(args);
	    }
	    
	    public Stage getStage() {
			return primaryStage;
		}
}