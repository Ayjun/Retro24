package gui.controller;

import static common.util.StringUtil.*;

import common.config.InstructionInfoConfig;
import common.config.MemoryDumpConfig;
import common.util.validate.FilePathValidator;
import common.util.validate.MemoryDumpValidator;
import core.Retro24;
import gui.controller.handler.ControlPanelEventHandler;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 * @author Eric Schneider
 */
public class ControlPanelController {

	// Der maximale Speicherbereich der beim Memorydump ausgegeben werden kann
	public static final int MAXMEMDUMP = 0x1FF;
	
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
	
	// Validators:
	private MemoryDumpValidator memDumpValidator;

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
	
	// Eventhandler:
	private ControlPanelEventHandler eventHandler;
	
	/**
	 * Methode die durch javaFX automatisch aufgerufen wird, nachdem das Controller-Objekt
	 * erstellt wurde. Hier werden Dinge die direkt nach Erzeugung des Objekts nötig sind erledigt.
	 */
	public void initialize() {
		initStyle();
		memDumpValidator = new MemoryDumpValidator(Retro24.MEMORY_START, Retro24.MEMORY_END, MAXMEMDUMP);
		eventHandler = new ControlPanelEventHandler(this);
		initDefaultValues();
		bindInternalProperties();
	}
	
	/**
	 * Methode zum Erledigen von Aktivitäten nach dem Anlegen des ScreenView Objekts
	 * aber VOR dem tatsächlichen Start der ScreenView.
	 */
	private void runBeforeScreenViewStart() {
		// CPU auf Pause setzen falls Haken bei CPU halten:
		cpuPausedBP().set(haltCPUCheckBoxBP().get());
		eventHandler.setCpuStepHandler(() -> screenViewController.stepCPU());
		bindExternalProperties();
		setUpExternalListeners();
	}
	
	/**
	 * Setzen von Style Einstellungen
	 */
	public void initStyle() {
	    // CSS-Klasse zuweisen
	    memoryDumpListView.getStyleClass().add("memory-dump-list-view");
	    instructionInfoListView.getStyleClass().add("memory-dump-list-view");
	    
	    // CSS-Datei laden
	    String cssPath = getClass().getResource("/resources/styles/list-view-styles.css").toExternalForm();
	    memoryDumpListView.getStylesheets().add(cssPath);
	    instructionInfoListView.getStylesheets().add(cssPath);
	}
	
	
	public void initDefaultValues() {
		vonMemoryDumpInput.setText(String.format("0x%04X",Retro24.PROGRAMM_MEMORYSTART));
		bisMemoryDumpInput.setText(String.format("0x%04X",Retro24.PROGRAMM_MEMORYSTART + 0xFF));
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
	
	/**
	 * Behandelt den Click auf den Start Button im Retro24 ControlPanel
	 */
	@FXML 
	public void handleStartButtonClick() {
		clearListViews();
		closeOldScreens();
		
		// Vor Validierung input Möglichkeiten deaktivieren (s.o. langer Kommentar)
		if (inputInactiveBP.isBound()) {
			inputInactiveBP.unbind();
		}
		inputInactiveBP.set(true);
		
		// Validieren der Eingaben
		if (!validateMemoryDump() || !validateProgramPath()) {
			inputInactiveBP.set(false);
			return;
		}
		
		// MemoryDumpConfig erstellen:
		MemoryDumpConfig memoryDumpConfig = makeMemoryDumpConfig();
		// InstructionInfoConfig erstellen:
		InstructionInfoConfig instructionInfoConfig = new InstructionInfoConfig(instructionInfoCheckBoxBP().get());
		
		// Neuen ScreenViewController (steuert Retro24 Instanz) anlegen:
		screenViewController = new ScreenViewController(this, pathInputTextSP().get(), memoryDumpConfig, instructionInfoConfig);
		
		runBeforeScreenViewStart();
		
		// Bildschirm des Retro24 anzeigen:
		screenViewController.showScreen();
	}
	
	private MemoryDumpConfig makeMemoryDumpConfig() {
		int startAddress = hexStringToInt(memoryDumpInputFromSP().get());
		int endAddress = hexStringToInt(memoryDumpInputToSP().get());
		return new MemoryDumpConfig(memoryDumpCheckBoxBP.get(), startAddress, endAddress);
	}
	
	/**
	 * Prüft die Addressen für den Memory Dump auf Gültigkeit.
	 * @return true wenn gültig, sonst false
	 */
	private boolean validateMemoryDump() {
		try {
			int startAddress = hexStringToInt(memoryDumpInputFromSP().get());
			int endAddress = hexStringToInt(memoryDumpInputToSP().get());
			
			if (!memDumpValidator.validateAddress(startAddress)) {
				controlPanelView.showError("Ungültige Startaddresse!",
						"Die Startaddresse liegt außerhalb des gültigen Bereichs.");
				return false;
			}
			
			if (!memDumpValidator.validateAddress(endAddress)) {
				controlPanelView.showError("Ungültige Endaddresse!",
						"Die Endaddresse liegt außerhalb des gültigen Bereichs.");
				return false;
			}
			
			if (!memDumpValidator.validateAddressRange(startAddress, endAddress)) {
				controlPanelView.showError("Ungültiger Addressbereich!",
						"Der gewählte Addressbereich ist zu groß oder ungültig." + System.lineSeparator() +
						"Der Addresbereich darf maximal " + String.format("0x%04X", memDumpValidator.getMaxRange())  + 
						" groß sein.");
				return false;
			}
		}
		catch (IllegalArgumentException e) {
			controlPanelView.showError("Ungültige Addresse!",
					"Bitte Zahlenwerte in hexadezimal Schreibweise (0x) verwenden." + System.lineSeparator() +
					"Bsp.: 0x1ABC");
			return false;
		}

		return true;
	}
	
	/**
	 * Prüft den im Input Feld angegebenen Programmpfad auf Gültigkeit.
	 * @return true wenn gültig, sonst false
	 */
	private boolean validateProgramPath() {
		FilePathValidator filePathValidator = new FilePathValidator(pathInputTextSP().get(), Retro24.SUPPORTED_FILE_EXTENSION);
		try {
			return filePathValidator.validate();
		} catch (IllegalArgumentException e) {
			controlPanelView.showError("Ungültige Datei!", "Die Datei konnte nicht gefunden werden, oder ist nicht unterstützt.");
			return false;
		}
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
 	
 	public Button getCpuStepButton() {
 		return cpuStepButton;
 	}
 	
 	public ListView<String> getMemoryDumpListView() {
 		return memoryDumpListView;
 	}
 	
 	public ListView<String> getInstructionInfoListView() {
 		return instructionInfoListView;
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
