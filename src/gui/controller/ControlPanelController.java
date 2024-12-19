package gui.controller;

import java.io.File;

import static util.NumberUtil.*;
import static util.StringUtil.*;
import static util.JavaFXUtil.*;

import gui.view.ControlPanelView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

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
	private TextField pathInputText;
	@FXML
	private Text vonMemoryDumpText;
	@FXML
	private Text bisMemoryDumpText;
	@FXML
	private Text einrastenText;
	
	// Referenz auf View:
	private ControlPanelView controlPanelView;
	
	// Referenz auf ScreenViewController:
	private ScreenViewController screenViewController;

	// BooleanProperties der Checkboxen (beim Start sind sie false):
	private BooleanProperty instructionInfoCheckBoxBP = new SimpleBooleanProperty(false);
	private BooleanProperty memoryDumpCheckBoxBP = new SimpleBooleanProperty(false);
	private BooleanProperty haltCPUCheckBoxBP = new SimpleBooleanProperty(false);
	
	// BooleanProperty ob die CPU grade im Moment pausiert ist:
	private BooleanProperty cpuPausedBP = new SimpleBooleanProperty(false);

	// Boolean Property dass die Input-Möglichkeiten des Users freigibt oder sperrt,
	// dies wird später gebunden an das systemRunningBP des screenViewController.
	// Läuft das System, sind keine Änderungen möglich. Das inputInactive wird
	// jedoch schon true geschaltet, kurz bevor systemRunningBP gebunden wird,
	// um zu vermeiden, dass während dieser kurzen Zwischenzeit (start Drücken und bevor
	// systemRunning) keine Änderungen an den Values wie den Checkboxen erfolgen können, 
	// da deren Werte teilweise live während des Laufens des Systems abgefragt werden.
	private BooleanProperty inputInactiveBP = new SimpleBooleanProperty(false);
	
	// String Properties:
	// Properties vonMemoryDumpInput und bisMemoryDumpInput
	private StringProperty memoryDumpInputFromSP = new SimpleStringProperty();
	private StringProperty memoryDumpInputToSP = new SimpleStringProperty();
	
	// Property von pathInputText (Dateipfadeingabe)
	private StringProperty pathInputTextSP = new SimpleStringProperty();
	
	/**
	 * Methode die durch javaFX automatisch aufgerufen wird, nachdem das Controller-Objekt
	 * erstellt wurde. Hier werden Dinge die direkt nach Erzeugung des Objekts nötig sind erledigt.
	 */
	public void initialize() {
		initStyle();
		bindInternalProperties();
		setUpInternalListeners();
	}
	
	/**
	 * Methode zum Erledigen von Aktivitäten nach dem Anlegen des ScreenView Objekts
	 * aber VOR dem tatsächlichen Start der ScreenView.
	 */
	private void runBeforeScreenViewStart() {
		initHandlers();
		bindExternalProperties();
		setUpExternalListeners();
	}
	
	/**
	 * Setzen von Style Einstellungen
	 */
	public void initStyle() {
		// LISTVIEWS (memoryDump und instructionInfo):
		// Setzen der CSS-Eigenschaften für den Text (monospace, 13px) der ListViews
		instructionInfoListView.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
		memoryDumpListView.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
		
		// Alle instructionInfo Tabellenzeilen sollen weißen Hintergrund haben:
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

		// Alle memoryDump Tabellenzeilen sollen weißen Hintergrund haben:
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
	 * Binden von lokalen (in diesem controlPanelController vorhandenen) Properties
	 */
	public void bindInternalProperties() {
		
		// Die Checkboxen an ihre BooleanProperties binden:
		memoryDumpCheckBoxBP.bind(memoryDumpCheckBox.selectedProperty());
		instructionInfoCheckBoxBP.bind(instructionInfoCheckBox.selectedProperty());
		haltCPUCheckBoxBP.bind(haltCPUCheckBox.selectedProperty()); // Achtung wegen evtl. invertieren!
		
		// Die CheckBoxBooleanProperties an ihre Sichtbarkeits-Abängigkeiten binden:
		// memoryDump Sichtbarkeitsabhängigkeiten:
		vonMemoryDumpInput.visibleProperty().bind(memoryDumpCheckBoxBP);
		vonMemoryDumpText.visibleProperty().bind(memoryDumpCheckBoxBP);
		bisMemoryDumpInput.visibleProperty().bind(memoryDumpCheckBoxBP);
		bisMemoryDumpText.visibleProperty().bind(memoryDumpCheckBoxBP);
		memoryDumpListView.visibleProperty().bind(memoryDumpCheckBoxBP);
		vonMemoryDumpInput.visibleProperty().bind(memoryDumpCheckBoxBP);
		bisMemoryDumpInput.visibleProperty().bind(memoryDumpCheckBoxBP);
		// cpuInstruction Sichtbarkeitsabhängigkeiten:
		instructionInfoListView.visibleProperty().bind(instructionInfoCheckBoxBP);
		// haltCPU Sichtbarkeitsabhängigkeiten:
		cpuStepButton.visibleProperty().bind(haltCPUCheckBoxBP);
		cpuStepButtonImage.visibleProperty().bind(haltCPUCheckBoxBP);
		einrastenText.visibleProperty().bind(haltCPUCheckBoxBP);
		
		// Die InputFelder an ihre StringProperties binden:
		memoryDumpInputToSP.bind(bisMemoryDumpInput.textProperty());
		memoryDumpInputFromSP.bind(vonMemoryDumpInput.textProperty());
		pathInputTextSP.bind(pathInputText.textProperty());
		
		// Inaktivität (disableProperty) der Input-Elemente an inputInactiveBP binden
		haltCPUCheckBox.disableProperty().bind(inputInactiveBP());
		instructionInfoCheckBox.disableProperty().bind(inputInactiveBP());
		memoryDumpCheckBox.disableProperty().bind(inputInactiveBP());
		vonMemoryDumpInput.disableProperty().bind(inputInactiveBP());
		bisMemoryDumpInput.disableProperty().bind(inputInactiveBP());
	}
	
	/**
	 * Erstellen von lokalen (innerhalb des controlPanelController) Listenern
	 */
	public void setUpInternalListeners() {
	// Listener auf Änderung der CPU-halten Checkbox um CPU-Pause zu setzen:
			haltCPUCheckBoxBP().addListener((obs, oldValue, newValue) -> {
				if (newValue) {
					cpuPausedBP().set(true);
				}
			});
	}
	
	/**
	 * Bindet alle externen Properties (extern = aus anderen Objekten)
	 */
	public void bindExternalProperties() {
		bindExternalPropertiesScreenViewController();
	}
	
	/**
	 * Binden von externen (im screenViewController vorhandenen) Properties
	 */
	public void bindExternalPropertiesScreenViewController() {
		
		// Die inputInactiveBP an systemRunningBP aus screenViewController binden s.o. langer Kommentar
		inputInactiveBP.bind(screenViewController.systemRunningBP());
		
		// Die Logs auf die Listview schalten
		instructionInfoListView.setItems(screenViewController.getInstructionLogObservable());
		memoryDumpListView.setItems(screenViewController.getMemoryLogObservable());
	}
	
	/**
	 * Setzen von Listenern mit externen Abhängigkeiten
	 */
	public void setUpExternalListeners() {
		// Listener auf screenViewController.systemRunningBP, 
		// um das Binding von inputInactiveBP zu entfernen, wenn sich systemRunningBP auf false ändert.
		// Denn falls wir nochmal den Startknopf drücken muss es wegen inputInactiveBP.set() unbinded sein.
		screenViewController.systemRunningBP().addListener((obs, oldValue, newValue) -> {
            if (!newValue) { // Wenn `systemRunningBP` auf `false` wechselt
                inputInactiveBP.unbind();
            }
        });
		
		// Listener auf neue Listeneinträge für Autoscroll:
		// instructionInfo automatisch herunterscrollen
		instructionInfoListView.getItems().addListener((ListChangeListener<String>) change -> {
		    tailInstructionListView();
		});
		
		// memoryInfo automatisch herunterscrollen
		memoryDumpListView.getItems().addListener((ListChangeListener<String>) change -> {
		    tailMemoryDumpListView();
		});
		
	}
		
	public void initHandlers() {
    
        // CPU Step Button Handler:
        cpuStepButton.setOnMouseClicked((event) -> {
        	// Wenn die CPU aktuell pausiert ist und Rechtsklick:
        	if (event.getButton() == MouseButton.SECONDARY && cpuPausedBP().get()) {
        		cpuPausedBP().set(false);
        	}
        	// Wenn die CPU aktuell nicht pausiert ist und Rechtsklick:
        	else if (event.getButton() == MouseButton.SECONDARY && !cpuPausedBP().get()) {
        		cpuPausedBP().set(true);
        	}
        	// Wenn die CPU pausiert ist und Linksklick:
        	else if (event.getButton() == MouseButton.PRIMARY && cpuPausedBP().get()) {
				screenViewController.stepCPU();
				screenViewController.drainLogs();
        	}
        	// Wenn die CPU nicht pausiert ist und Linksklick:
        	else if (event.getButton() == MouseButton.PRIMARY && !cpuPausedBP().get()) {
        		cpuPausedBP().set(true);
        	}
        });
        
        // ListViews sollen bei Mausinteraktion NUR auf die Scrollbar reagieren:
        // Für memoryDumpListView
        memoryDumpListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isScrollBar(event.getTarget())) {
                event.consume(); // Alle anderen Klicks blockieren
            }
        });
        
        // Für instructionInfoListView
        instructionInfoListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isScrollBar(event.getTarget())) {
                event.consume(); // Alle anderen Klicks blockieren
            }
        });
	}
	
	/**
	 * Behandelt den Click auf den Start Button im Retro24 ControlPanel
	 */
	@FXML 
	public void handleStartButtonClick() {
		clearListViews();
		closeOldScreens();
		
		// Vor Validierung input Möglichkeiten deaktivieren (s.o. langer Kommentar)
		inputInactiveBP.set(true);
		if (!validateValues()) {
			// Falls ungültiger Wert wieder freigeben:
			inputInactiveBP.set(false);
			return;
		}

		// Neuen ScreenViewController (steuert Retro24 Instanz) anlegen:
		screenViewController = new ScreenViewController(this, pathInputTextSP().get());
		
		runBeforeScreenViewStart();
		
		// Bildschirm des Retro24 anzeigen:
		screenViewController.showScreen();
	}
	
	/**
	 * Validiert die Gültigkeit der Parameter auf die der ScreenViewController zugreift /
	 * die an ihn übergeben werden.
	 * @return 
	 */
	private boolean validateValues() {
		return validateProgramPath() && validateMemoryDumpAddresses();
	}

	/**
	 * Validiert die Gültigkeit der Speicheradressen in den entsprechenden Input Feldern,
	 * sowie die Gültigkeit der Addressrange.
	 * @return true wenn gülitg, sonst false
	 */
	private boolean validateMemoryDumpAddresses() {
		// Wenn Checkbox inaktiv:
		if (!memoryDumpCheckBoxBP.get()) {
			return true;
		}

		if (!validateMemoryDumpStartAddress() || !validateMemoryDumpEndAddress()) {
			return false;
		}
		
		Integer start = hexStringToInt(memoryDumpInputFromSP.get());
		Integer end = hexStringToInt(memoryDumpInputToSP.get());
		
		if (start > end) {
			controlPanelView.showError("Ungültige Adresse!", "Startadresse ist größer als Endaddrese!");
			return false;
		}
		
		if (end - start > MAXMEMDUMP) {
			controlPanelView.showError("Ungültige Adressraum!", "Maximaler Addressbereich von " + intToHexString(MAXMEMDUMP) +  
					" ist überschritten!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validiert die Gültigkeit der memoryDumpStartAddress aus dem memoryDumpInputFromSP Input Feld.
	 * @return true wenn gülitg, sonst false
	 */
	private boolean validateMemoryDumpStartAddress() {
		// Wenn Checkbox inaktiv:
		if (!memoryDumpCheckBoxBP.get()) {
			return true;
		}
		
		Integer address = null;
 		try {
 			address = hexStringToInt(memoryDumpInputFromSP().get());
 		}
 		catch (IllegalArgumentException e) {
 			controlPanelView.showError("Ungültige Adresse!", e.getMessage());
 			return false;
 		}
 		if (checkShortOverflow(address) || address == null || checkUnderflow(address)) {
 			controlPanelView.showError("Ungültige Adresse!", "Adresse liegt nicht im zulässigen Speicherbereich: " + address);
 			return false;
 		} 

 		return true;
	}
	
	/**
	 * Validiert die Gültigkeit der memoryDumpStartAddress aus dem memoryDumpInputFromSP Input Feld.
	 * @return true wenn gülitg, sonst false
	 */
	private boolean validateMemoryDumpEndAddress() {
		// Wenn Checkbox inaktiv:
		if (!memoryDumpCheckBoxBP.get()) {
			return true;
		}
		
		Integer address = null;
 		try {
 			address = hexStringToInt(memoryDumpInputToSP().get());
 		}
 		catch (IllegalArgumentException e) {
 			controlPanelView.showError("Ungültige Adresse!", e.getMessage());
 			return false;
 		}
 		if (checkShortOverflow(address) || address == null || checkUnderflow(address)) {
 			controlPanelView.showError("Ungültige Adresse!", "Adresse liegt nicht im zulässigen Speicherbereich: " + address);
 			return false;
 		} 
 		return true;
	}
	
	/**
	 * Validiert die Gültigkeit des Programmpfads in den entsprechenden Input Feldern.
	 * @return true wenn gülitg, sonst false
	 */
	private boolean validateProgramPath() {
		File file = new File(pathInputTextSP().get());
		if (!file.exists()) return false;
		if (!file.getName().endsWith(".bin")) return false;
		return true;
	}

	/**
	 * Wenn es bereits einen screenViewController gab, schließe seine ScreenView
	 * und lösche die Referenz.
	 */
	public void closeOldScreens() {
		if (screenViewController == null) {
			return;
		}
		screenViewController.closeScreen();
		screenViewController = null;
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
	 * Behandelt den Click auf ... Button zum Programmpfad suchen
	 */
	public void handleLookForFileButtonClick() {
		String path = controlPanelView.openFileDialog();
		pathInputText.setText(path);
	}
		
	/**
	 * Scrollt in allen ListViews ganz nach unten.
	 */
	public void tail() {
 		tailMemoryDumpListView();
 		tailInstructionListView();
 	}
	
	/**
	 * Scrollt in der memoryDumpListView ganz nach unten
	 */
	public void tailMemoryDumpListView() {
		if (!memoryDumpCheckBoxBP().get()  || memoryDumpListView.getItems() == null) {
			return;
		}
		Platform.runLater(() -> {
			memoryDumpListView.scrollTo(memoryDumpListView.getItems().size());
		});
	}
	
	/**
	 * Scrollt in der instructionListView ganz nach unten
	 */
	public void tailInstructionListView() {
		if (!instructionInfoCheckBoxBP().get() || instructionInfoListView.getItems() == null) {
			return;
		}
		Platform.runLater(() -> {
			instructionInfoListView.scrollTo(instructionInfoListView.getItems().size());
		});
	}
 	
 	/**
 	 * Wandelt den Wert im memoryDumpInputFrom Feld in einen Integer Wert um und gibt ihn zurück,
 	 * prüft vorher die Adresse auf Gültigkeit.
 	 * @return Integer Repräsentation oder null bei ungültiger Adresse
 	 */
 	public Integer getMemoryDumpStartAddress() {
 		if (!validateMemoryDumpStartAddress()) {
 			return null;
 		}
 	    return hexStringToInt(memoryDumpInputFromSP().get());
 	}
 	
 	/**
 	 * Wandelt den Wert im memoryDumpInputTo Feld in einen Integer Wert um und gibt ihn zurück,
 	 * prüft hierbei die Adresse auf Gültigkeit.
 	 * @return Integer Repräsentation oder null bei ungültiger Adresse
 	 */
 	public Integer getMemoryDumpEndAddress() {
 		if (!validateMemoryDumpEndAddress()) {
 			return null;
 		}
 	    return hexStringToInt(memoryDumpInputToSP().get());
 	}
 	
 	/**
 	 * Cleared alle ListViews
 	 */
 	public void clearListViews() {
 		clearMemoryDumpListView();
 		clearInstructionInfoListView();
 	}
 	
 	/**
 	 * Cleared die memoryDumpListView
 	 */
 	public void clearMemoryDumpListView() {
 		if (!memoryDumpCheckBoxBP().get() || memoryDumpListView.getItems() == null) {
 			return;
 		}
 		memoryDumpListView.getItems().clear();
 	}
 	
 	/**
 	 * Cleared die instructionInfoListView
 	 */
 	public void clearInstructionInfoListView() {
 		if (!instructionInfoCheckBoxBP.get() || instructionInfoListView.getItems() == null) {
 	 		return;
 		}
 		instructionInfoListView.getItems().clear();
 	}
 	
 	public BooleanProperty instructionInfoCheckBoxBP() {
 		return instructionInfoCheckBoxBP;
 	}
 	
 	public BooleanProperty memoryDumpCheckBoxBP() {
 		return memoryDumpCheckBoxBP;
 	}
 	
 	public BooleanProperty haltCPUCheckBoxBP() {
 		return haltCPUCheckBoxBP;
 	}
 	
 	public BooleanProperty cpuPausedBP() {
 		return cpuPausedBP;
 	}

 	public BooleanProperty inputInactiveBP() {
 		return inputInactiveBP;
 	}
 	
 	private StringProperty memoryDumpInputFromSP() {
 		return memoryDumpInputFromSP;
 	}
 	
 	private StringProperty memoryDumpInputToSP() {
 		return memoryDumpInputToSP;
 	}
 	
 	private StringProperty pathInputTextSP() {
 		return pathInputTextSP;
 	}
 	
 	/**
 	 * Übergibt die controlPanelView Referenz an dieses Objekt (den ControlPanelController).
 	 * @param controlPanelView
 	 */
	public void setControlPanelView(ControlPanelView controlPanelView) {
		this.controlPanelView = controlPanelView;
	}
}
