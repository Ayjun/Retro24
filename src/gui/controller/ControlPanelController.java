package gui.controller;

import java.io.File;
import java.io.FileNotFoundException;

import gui.view.ControlPanelView;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class ControlPanelController {

	@FXML
	private TextField pathInputText;
	private ControlPanelView controlPanelView;
	private ScreenViewController sc;
	
	public void setControlPanelView(ControlPanelView controlPanelView) {
		this.controlPanelView = controlPanelView;
	}

	/**
	 * Behandelt den Click auf das Pfadeingabefeld im Retro24 ControlPanel
	 */
	@FXML
	boolean pathInputWasClicked = false;
	public void handlePathInputTextClick() {
		if (pathInputWasClicked) {
			return;
		}
		pathInputText.setText("");
		pathInputWasClicked = true;
	}
	
	/**
	 * Behandelt den Click auf den Start Button im Retro24 ControlPanel
	 * @throws FileNotFoundException 
	 */
	@FXML 
	public void handleStartButtonClick() throws FileNotFoundException {
		String programPath = pathInputText.getText(); // Pfad des zu startenden Programmes
		
		File f = new File(programPath);
		if (!f.isFile()) {
			controlPanelView.showError("Programm nicht gefunden!", "Das Programm:\n\n" + programPath + "\n\nkonnte nicht gefunden werden!");
			return;
		}
		
		// Wenn es bereits einen screencontroller gab, dann schließe die Stage der zugehörigen View
		// (Altes Retro24 Fenster schliessen)
		if (sc!= null) {
			sc.screenView.getStage().close();
		}
		
		
		sc = new ScreenViewController();
		sc.setProgramPath(programPath);
		sc.screenView.showRetro24Screen(sc);
	}
	
	public void handleLookForFileButtonClick() {
		String path = openFileDialog();
		pathInputText.setText(path);
	}
	
 	public String openFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datei auswählen");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("binaries", "*.bin"));

        File selectedFile = fileChooser.showOpenDialog(controlPanelView.getStage());
        if (selectedFile != null) {
           return selectedFile.getAbsolutePath();
        }
		return null;
    }
}
