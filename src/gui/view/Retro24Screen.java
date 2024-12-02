package gui.view;

import gui.controller.Retro24Controller;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Retro24Screen extends Application {

    @Override
    public void start(Stage primaryStage) {

        // GUI-Layout erstellen
        BorderPane root = new BorderPane();
        
        Retro24Controller sc = new Retro24Controller();
        
        root.setCenter(sc.screenView.getCanvas());
        
        

        // Szene und Stage konfigurieren
        Scene scene = new Scene(root, 650, 650); // Größe anpassen
        primaryStage.setTitle("Retro24");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
