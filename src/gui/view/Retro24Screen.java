package gui.view;

import gui.controller.Retro24Controller;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

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
        
        // Pause für 3 Sekunden, bevor das System gestartet wird
        // um den Willkommensbildschirm zu zeigen
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> sc.runSystem());
        pause.play();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
