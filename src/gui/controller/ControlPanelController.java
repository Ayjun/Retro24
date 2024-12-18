package gui.controller;

import java.io.File;

import core.Retro24;

import static util.NumberUtil.*;

import gui.view.ControlPanelView;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 * @author Eric Schneider
 */
public class ControlPanelController {

	// Der maximale Speicherbereich der beim Memorydump ausgegeben werden kann
	private static int MAXMEMDUMP = 0x1FF;
	
	// FXML Attribute (SceneBuilder):
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
	private ListView<String> instructionInfoListView;
	@FXML
	private ListView<String> memoryDumpListView;
	@FXML
	private TextField vonMemoryDumpInput;
	@FXML
	private TextField bisMemoryDumpInput;
	@FXML
	private Text vonMemoryDumpText;
	@FXML
	private Text bisMemoryDumpText;
	@FXML
	private Text einrastenText;
	
	// Referenz auf View:
	private ControlPanelView controlPanelView;
	// Referenz auf ScreenViewController:
	private ScreenViewController sc;
	
	// Enthält Info darüber, ob CPU laufen soll:
	private BooleanProperty cpuPaused = new SimpleBooleanProperty(false);
	
	/**
	 * Methode zum Erledigen von Dingen vor dem eigentlichen Systemstart (im ControlPanelController).
	 */
	public void beforeRunControlPanelController() {
		initHandlers();
		bindProperties();
		initStyle();	
	}
	
	/**
	 * Setzen von Style Einstellungen
	 */
	public void initStyle() {
		// LISTVIEWS (memoryDump und instructionInfo):
		
		// Setzen der CSS-Eigenschaften für den Text (monospace, 13px) der ListViews
		instructionInfoListView.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
		memoryDumpListView.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
		
		// Alle Tabellenzeilen sollen weißen Hintergrund haben:
		instructionInfoListView.setCellFactory(lv -> new ListCell<>() {
		    @Override
		    protected void updateItem(String item, boolean empty) {
		        super.updateItem(item, empty);

		        if (empty || item == null) {
		            setText(null);
		            setStyle("-fx-background-color: white;"); // Hintergrundfarbe für leere Zellen
		        } else {
		            setText(item);
		            setStyle("-fx-background-color: white;"); // Hintergrundfarbe und Schrift
		        }
		    }
		});

		// Alle Tabellenzeilen sollen weißen Hintergrund haben:
		memoryDumpListView.setCellFactory(lv -> new ListCell<>() {
		    @Override
		    protected void updateItem(String item, boolean empty) {
		        super.updateItem(item, empty);

		        if (empty || item == null) {
		            setText(null);
		            setStyle("-fx-background-color: white;"); // Hintergrundfarbe für leere Zellen
		        } else {
		            setText(item);
		            setStyle("-fx-background-color: white;"); // Hintergrundfarbe und Schrift
		        }
		    }
		});
	}
	
	/**
	 * Binden von Properties
	 */
	public void bindProperties() {
		haltCPUCheckBox.disableProperty().bind(sc.isSystemRunningProperty());
		instructionInfoCheckBox.disableProperty().bind(sc.isSystemRunningProperty());
		memoryDumpCheckBox.disableProperty().bind(sc.isSystemRunningProperty());
		vonMemoryDumpInput.disableProperty().bind(sc.isSystemRunningProperty());
		bisMemoryDumpInput.disableProperty().bind(sc.isSystemRunningProperty());
		instructionInfoListView.setItems(sc.getInstructionLogObservable());
		memoryDumpListView.setItems(sc.getMemoryLogObservable());
		
		// TEST TEST
		instructionInfoListView.getItems().addListener((ListChangeListener<String>) change -> {
		    while (change.next()) {
		        if (change.wasAdded() || change.wasReplaced()) {
		            Platform.runLater(() -> instructionInfoListView.scrollTo(instructionInfoListView.getItems().size()));
		        }
		    }
		});
		
		memoryDumpListView.getItems().addListener((ListChangeListener<String>) change -> {
		    while (change.next()) {
		        if (change.wasAdded() || change.wasReplaced()) {
		            Platform.runLater(() -> memoryDumpListView.scrollTo(memoryDumpListView.getItems().size()));
		        }
		    }
		});
		
		// TEST ENDE
	}
	
	
	public void initHandlers() {
    
        // CPU Step Button Handler:
        cpuStepButton.setOnMouseClicked((event) -> {
        	// Wenn die CPU aktuell pausiert ist und Rechtsklick:
        	if (event.getButton() == MouseButton.SECONDARY && cpuPaused.get()) {
        		cpuPaused.set(false);
        	}
        	// Wenn die CPU aktuell nicht pausiert ist und Rechtsklick:
        	else if (event.getButton() == MouseButton.SECONDARY && !cpuPaused.get()) {
        		cpuPaused.set(true);
        	}
        	// Wenn die CPU pausiert ist und Linksklick:
        	else if (event.getButton() == MouseButton.PRIMARY && cpuPaused.get()) {
        		try {
					sc.stepCPU();
					sc.drainLogs();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	// Wenn die CPU nicht pausiert ist und Linksklick:
        	else if (event.getButton() == MouseButton.PRIMARY && !cpuPaused.get()) {
        		cpuPaused.set(true);
        	}
        });
        
        // ListViews sollen nicht auf Clicks reagieren:
        memoryDumpListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isScrollBar(event.getTarget())) {
                event.consume(); // Alle anderen Klicks blockieren
            }
        });
        instructionInfoListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isScrollBar(event.getTarget())) {
                event.consume(); // Alle anderen Klicks blockieren
            }
        });
	}
	
	// Hilfsmethode zur Prüfung, ob Ziel oder ein Elternteil eine ScrollBar ist
	private boolean isScrollBar(Object target) {
	    if (target instanceof javafx.scene.control.ScrollBar) {
	        return true;
	    }
	    if (target instanceof Node) {
	        Node node = (Node) target;
	        while (node != null) {
	            if (node instanceof javafx.scene.control.ScrollBar) {
	                return true;
	            }
	            node = node.getParent();
	        }
	    }
	    return false;
	}

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
			einrastenText.setVisible(true);
			cpuPaused().set(true);
			return;
		}
		cpuStepButton.setVisible(false);
		cpuStepButtonImage.setVisible(false);
		einrastenText.setVisible(false);
		cpuPaused().set(false);
	}
	
	@FXML
	public void handleCheckInstructionInfoCheckBox() {
		if (instructionInfoCheckBox.isSelected()) {
			instructionInfoListView.setVisible(true);
			return;
		}
		instructionInfoListView.setVisible(false);
	}
	
	@FXML
	public void handleCheckMemoryDumpCheckBox() {
		if (memoryDumpCheckBox.isSelected()) {
			vonMemoryDumpInput.setVisible(true);
			vonMemoryDumpText.setVisible(true);
			bisMemoryDumpInput.setVisible(true);
			bisMemoryDumpText.setVisible(true);
			memoryDumpListView.setVisible(true);
			vonMemoryDumpInput.setText("0x0100");
			bisMemoryDumpInput.setText("0x01FF");
			return;
		}
		vonMemoryDumpInput.setVisible(false);
		vonMemoryDumpText.setVisible(false);
		bisMemoryDumpInput.setVisible(false);
		bisMemoryDumpText.setVisible(false);
		memoryDumpListView.setVisible(false);
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
				controlPanelView.showError("Ungültiger Adressbereich!", "Der maximal erlaubte Adressbereich ist überschritten!");
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
		
		beforeRunControlPanelController();
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
 	
 	public void tail() {
 		Platform.runLater(() -> {
 	        memoryDumpListView.scrollTo(memoryDumpListView.getItems().size());
 	        instructionInfoListView.scrollTo(memoryDumpListView.getItems().size());
 		});
 	}
 	
 	
 	/**
 	 * Resettet die Textareas
 	 */
 	public void resetTextAreas() {
 		this.instructionInfoListView.getItems().clear();
 		this.memoryDumpListView.getItems().clear();
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
 	
 	public BooleanProperty cpuPaused() {
 		return this.cpuPaused;
 	}
}
