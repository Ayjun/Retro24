package gui.controller;

import java.io.File;

import core.Retro24;

import static util.DebugUtil.*;
import static util.NumberUtil.*;

import gui.view.ControlPanelView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

public class ControlPanelController {

	// Der maximale Speicherbereich der beim Memorydump ausgegeben werden kann
	private static int MAXMEMDUMP = 0xFF;
	
	@FXML
	private Button startButton;
	@FXML
	private TextField pathInputText;
	@FXML
	private Button lookForFileButton;
	@FXML
	private CheckBox instructionInfoCheckBox;
	@FXML
	private CheckBox memoryDumpCheckBox;
	@FXML
	private CheckBox haltCPUCheckBox;
	@FXML
	private Button cpuStepButton;
	@FXML
	private ImageView cpuStepButtonImage;
	@FXML
	private TextFlow instructionInfoFlow;
	@FXML
	private TextArea instructionInfoText;
	@FXML
	private TextFlow memoryDumpFlow;
	@FXML
	private TextArea memoryDumpText;
	@FXML
	private TextField vonMemoryDumpInput;
	@FXML
	private TextField bisMemoryDumpInput;
	@FXML
	private Text vonMemoryDumpText;
	@FXML
	private Text bisMemoryDumpText;
	
	private ControlPanelView controlPanelView;
	private ScreenViewController sc;

	
	public void setControlPanelView(ControlPanelView controlPanelView) {
		this.controlPanelView = controlPanelView;
	}
	
	/** 
	 * Behandelt checken der CPU halten Checkbox
	 */
	@FXML
	public void handleCheckHaltCPUCheckBox() {
		if (haltCPUCheckBox.isSelected()) {
			cpuStepButton.setVisible(true);
			cpuStepButtonImage.setVisible(true);
			return;
		}
		cpuStepButton.setVisible(false);
		cpuStepButtonImage.setVisible(false);
	}
	
	@FXML
	public void handleCheckInstructionInfoCheckBox() {
		if (instructionInfoCheckBox.isSelected()) {
			instructionInfoFlow.setVisible(true);
			instructionInfoText.setVisible(true);
			return;
		}
		instructionInfoFlow.setVisible(false);
		instructionInfoText.setVisible(false);
	}
	
	@FXML
	public void handleCheckMemoryDumpCheckBox() {
		if (memoryDumpCheckBox.isSelected()) {
			vonMemoryDumpInput.setVisible(true);
			vonMemoryDumpText.setVisible(true);
			bisMemoryDumpInput.setVisible(true);
			bisMemoryDumpText.setVisible(true);
			memoryDumpFlow.setVisible(true);
			memoryDumpText.setVisible(true);
			return;
		}
		vonMemoryDumpInput.setVisible(false);
		vonMemoryDumpText.setVisible(false);
		bisMemoryDumpInput.setVisible(false);
		bisMemoryDumpText.setVisible(false);
		memoryDumpFlow.setVisible(false);
		memoryDumpText.setVisible(false);
	}
	
	@FXML
	public void handleClickCPUStepButton() {
		synchronized (sc.pauseMonitor) {
	        sc.isPaused = !sc.isPaused; // Pause umschalten
	        if (!sc.isPaused) {
	            sc.pauseMonitor.notify(); // Weitermachen
	        }
		}
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
	 */
	@FXML 
	public void handleStartButtonClick() {
		
		resetTextAreas();
		
		// Wenn es bereits einen screencontroller gab, dann schließe die Stage der zugehörigen View
		// (Altes Retro24 Fenster schliessen)
		if (sc!= null) {
			sc.closeScreen();
		}

		// Neuen ScreenViewController (steuert Retro24 Instanz) anlegen:
		sc = new ScreenViewController(this);
		
		if (!loadProgram()) {
			return;
		}
		
		// Falls MemoryDump Checkbox angehakt, die erforderlichen Infos im ScreenViewController setzen:
		if (isDumpMemorySelected()) {
			
			if (getMemoryAddressFrom() == null || getMemoryAddressTo() == null) {
				return;
			}
			
			if (getMemoryAddressTo() - getMemoryAddressFrom() > MAXMEMDUMP) {
				controlPanelView.showError("Ungültiger Adressbereich!", "Der maximal erlaubte Adressbereich ist 0xFF lang.");
				return;
			}
			
			if (checkShortOverflow(getMemoryAddressTo()) || checkShortOverflow(getMemoryAddressFrom())
					|| checkUnderflow(getMemoryAddressTo()) || checkUnderflow(getMemoryAddressFrom())) {
				controlPanelView.showError("Ungültige Adresse!", "Die Adressen müssen im Bereich " 
					+ String.format("0x%02X", Retro24.MEMORYSTART)  
					+ " bis " 
					+ String.format("0x%02X", Retro24.MEMORYEND) 
					+ " liegen!");
				return;
			}
			
			sc.setDumpMemory(getMemoryAddressFrom(), getMemoryAddressTo());
		}
		
		// Bildschirm des Retro24 anzeigen:
		sc.showScreen();
	
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
 	
 	/**
 	 * Hängt der MemoryDump TextArea den übergebenen String an.
 	 * @param memoryDump
 	 */
 	public void updateMemoryDumpTextArea(String memoryDump) {
 		if (!memoryDumpCheckBox.isSelected()) {
 			return;
 		}
 	
 		memoryDumpText.appendText(memoryDump);
 	}
 	
 	/**
 	 * Setzt die MemoryDump TextArea auf den übergebenen String.
 	 * @param memoryDump
 	 */
 	public void setMemoryDumpTextArea(String memoryDump) {
 		if (!memoryDumpCheckBox.isSelected()) {
 			return;
 		}
 		memoryDumpText.setText(memoryDump);
 	}
 	
 	
 	/**
 	 * Hängt der Instruction Info TextArea den übergebenen String an.
 	 * @param memoryDump
 	 */
 	public void updateInstructionInfoTextArea(String instructionInfo) {
 		if (!instructionInfoCheckBox.isSelected()) {
 			return;
 		}
 		instructionInfoText.appendText(instructionInfo);
 	}
 	
 	/**
 	 * Setzt die Instruction Info TextArea auf den übergebenen String.
 	 * @param memoryDump
 	 */
 	public void setInstructionInfoTextArea(String instructionInfo) {
 		if (!memoryDumpCheckBox.isSelected()) {
 			return;
 		}
 		instructionInfoText.setText(instructionInfo);
 	}
 	
 	
 	
 	public void setScreenViewController(ScreenViewController screenViewController) {
 		this.sc = screenViewController;
 	}
 	

 	
 	/**
 	 * Lädt ein Prorgramm vom angegebenen Pfad (Textinput)
 	 * @return true wenn erfolgreich, false wenn nicht
 	 */
 	public boolean loadProgram() {
 		String programPath = pathInputText.getText(); // Pfad des zu startenden Programmes aus Input lesen
		
		File f = new File(programPath);
		
		if (!f.isFile()) {
			controlPanelView.showError("Programm nicht gefunden!", "Das Programm:\n\n" + programPath + "\n\nkonnte nicht gefunden werden!");
			return false;
		}
		
		sc.setProgramPath(programPath);
		return true;
 	}
 	
 	public Integer getMemoryAddressFrom() {
 		String address = this.vonMemoryDumpInput.getText();
 		
 		if (address == null || address.isEmpty()) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse darf nicht leer sein.");
 	    }

 	    if (!address.startsWith("0x")) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse muss mit '0x' beginnen.");
 	    }

 	    try {
 	        // Entferne den Präfix "0x" und parse den Rest als Hexadezimalwert
 	        return Integer.parseInt(address.substring(2), 16);
 	    } catch (Exception e) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse ist kein gültiger Hexadezimalwert: " + address.toString());
 	    }
 	    
 	    return null;
 	}
 	
 	public Integer getMemoryAddressTo() {
 		String address = this.bisMemoryDumpInput.getText();
 		
 	    if (address == null || address.isEmpty()) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse darf nicht leer sein.");
 	    	return null;
 	    }

 	   if (!address.startsWith("0x")) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse muss mit '0x' beginnen.");
 	    	return null;
 	    }

 	    try {
 	        // Entferne den Präfix "0x" und parse den Rest als Hexadezimalwert
 	        return Integer.parseInt(address.substring(2), 16);
 	    } catch (NumberFormatException e) {
 	    	controlPanelView.showError("Ungültige Adresse!", "Die Adresse ist kein gültiger Hexadezimalwert: " + address.toString());
 	    }
 	    
 	    return null;
 	}
 	
 	
 	/**
 	 * Resettet die Textareas
 	 */
 	public void resetTextAreas() {
 		this.instructionInfoText.clear();
 		this.memoryDumpText.clear();
 	}
 	
 	public void appendToMemoryDumpText(String dump) {
 		this.memoryDumpText.appendText(dump);
 	}
 	
 	public boolean isInstructionInfoSelected() {
 		return this.instructionInfoCheckBox.isSelected();
 	}
 	
 	public boolean isDumpMemorySelected() {
 		return this.memoryDumpCheckBox.isSelected();
 	}
 	
 	public boolean isCPUHaltenSelected() {
 		return this.haltCPUCheckBox.isSelected();
 	}
}
