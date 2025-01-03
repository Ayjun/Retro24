package gui.controller;

import core.Retro24;
import gui.controller.binder.ControlPanelBinder;
import gui.controller.handler.ControlPanelEventHandler;
import gui.controller.validator.ControlPanelValidator;
import gui.view.ControlPanelView;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 * Controller der ControlPanelView. Der Controller dient als Vermittler 
 * zwischen dem Retro24 Control Panel, der View und dem
 * Retro24 ScreenViewController.
 * @author Eric Schneider
 */
public class ControlPanelController {

	// Konstanten
	public static final int MAXMEMDUMP = 0x1FF;
	public static final String PROGRAMM_PATH_INPUT_DEFAULT = "Programmpfad";
	
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
	private final BooleanProperty instructionInfoCheckBoxBP = new SimpleBooleanProperty(false);
	private final BooleanProperty memoryDumpCheckBoxBP = new SimpleBooleanProperty(false);
	private final BooleanProperty haltCPUCheckBoxBP = new SimpleBooleanProperty(false);
	// BooleanProperty ob die CPU grade im Moment pausiert ist:
	private final BooleanProperty cpuPausedBP = new SimpleBooleanProperty(false);
	// Boolean Property dass die Input-Möglichkeiten des Users freigibt oder sperrt,
	// dies wird später gebunden an das systemRunningBP des screenViewController.
	// Läuft das System, sind keine Änderungen möglich. Das inputInactive wird
	// jedoch schon true geschaltet, kurz bevor systemRunningBP gebunden wird,
	// um zu vermeiden, dass während dieser kurzen Zwischenzeit (start Drücken und bevor
	// systemRunning) keine Änderungen an den Values wie den Checkboxen erfolgen können, 
	// da deren Werte teilweise live während des Laufens des Systems abgefragt werden.
	private final BooleanProperty inputInactiveBP = new SimpleBooleanProperty(false);
	
	// String Properties:
	// Properties vonMemoryDumpInput und bisMemoryDumpInput
	private final StringProperty memoryDumpInputFromSP = new SimpleStringProperty();
	private final StringProperty memoryDumpInputToSP = new SimpleStringProperty();
	
	// Property von pathInputText (Dateipfadeingabe)
	private final StringProperty pathInputTextSP = new SimpleStringProperty();
	
	// Eventhandler (Auslagerung der event-handling Logik des Controllers):
	private ControlPanelEventHandler eventHandler;
	// Eventhandler (Auslagerung der binding Logik des Controllers):
	private ControlPanelBinder binder;
	// Validator (Auslagerung der validierungs Logik des Controllers):
	private ControlPanelValidator validator;
	
	/**
	 * Methode die durch javaFX automatisch aufgerufen wird, nachdem das Controller-Objekt
	 * erstellt wurde. Hier werden Dinge die direkt nach Erzeugung des Objekts nötig sind erledigt.
	 * 1. TODO Aufnummerieren
	 */
	public void initialize() {
		initDefaultValues();
		
		// Anlegen von nötigen Komponenten
		binder = new ControlPanelBinder(this);
		eventHandler = new ControlPanelEventHandler(this);
		validator = new ControlPanelValidator(this);
		
		// Initialisieren der Komponenten
		binder.initializeBindings();
		
		eventHandler.initializeHandlers();
	}
	
	/**
	 * Methode zum Erledigen von Aktivitäten nach dem Anlegen des ScreenView Objekts
	 * aber VOR dem tatsächlichen Start der ScreenView. Z.B. Initialisierungen
	 * von Objekten welche externe Abhängigkeiten haben.
	 */
	public void runBeforeScreenViewStart() {
		// CPU auf Pause setzen falls Haken bei CPU halten:
		cpuPausedBP().set(haltCPUCheckBoxBP().get());
		
		// Die Methode die beim Links-Clicken auf die CPU-Step Taste aussgeführt
		// werden soll mittels eines CPUStepHandler (funktionales Interface)
		// erstellen und an den Eventhandler übergeben.
		// (Erst hier wegen Abhängigkeit Existenz screenViewController)
		eventHandler.setCpuStepHandler(() -> {
			screenViewController.stepCPU();
			screenViewController.drainLogs();
		});
		
		// Initialisieren von externen Bindings und Listenern
		binder.bindExternalProperties();
		binder.initializeExternalListeners();
	}
	
	/**
	 * Setzten der Felder der View auf initiale Anzeige
	 */
	public void initDefaultValues() {
		vonMemoryDumpInput.setText(String.format("0x%04X",Retro24.PROGRAMM_MEMORYSTART));
		bisMemoryDumpInput.setText(String.format("0x%04X",Retro24.PROGRAMM_MEMORYSTART + 0xFF));
	}

	/**
	 * Behandelt den Click auf den Start Button im Retro24 ControlPanel
	 */
	@FXML 
	public void handleStartButtonClick() {
		eventHandler.handleStartButtonClick();
	}
	
	/**
	 * Behandelt den Click auf das Pfadeingabefeld im Retro24 ControlPanel
	 * Falls noch der default Wert eingetragen ist, wird das Feld geleert.
	 */
	@FXML
	public void handlePathInputTextClick() {
		eventHandler.handlePathInputTextClick();
	}
	
	/**
	 * Behandelt den Click auf ... Button zum Programmpfad suchen
	 */
	public void handleLookForFileButtonClick() {
		eventHandler.handleLookForFileButtonClick();
	}
	
	/**
	 * Leitet eine Error Message weiter an die View,
	 * diese öffnet einen Errordialog.
	 * @param title Titel des Errordialog Fensters
	 * @param message Nachricht im Errordialog Fenster
	 */
	public void showError(String title, String message) {
		controlPanelView.showError(title, message);
	}
	
	/**
	 * Ruft die Methode openFileDialog der View auf,
	 * dies führt zum Öfnnen eines Dateiauswahldiealogs, 
	 * der zurückgegebene String wird durchgereicht.
	 * @return der Dateipfad als String
	 */
	public String openFileDialog() {
		return controlPanelView.openFileDialog();
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
 	
 	// Getter

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
 	
 	public StringProperty memoryDumpInputFromSP() {
 		return memoryDumpInputFromSP;
 	}
 	
 	public StringProperty memoryDumpInputToSP() {
 		return memoryDumpInputToSP;
 	}
 	
 	public StringProperty pathInputTextSP() {
 		return pathInputTextSP;
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
 	
 	public ImageView getCpuStepButtonImage() {
		return cpuStepButtonImage;
	}

	public TextField getVonMemoryDumpInput() {
		return vonMemoryDumpInput;
	}

	public TextField getBisMemoryDumpInput() {
		return bisMemoryDumpInput;
	}

	public Text getVonMemoryDumpText() {
		return vonMemoryDumpText;
	}

	public Text getBisMemoryDumpText() {
		return bisMemoryDumpText;
	}

	public Text getEinrastenText() {
		return einrastenText;
	}
	
	public Button getStartButton() {
		return startButton;
	}

	public Button getLookForFileButton() {
		return lookForFileButton;
	}

	public CheckBox getInstructionInfoCheckBox() {
		return instructionInfoCheckBox;
	}

	public CheckBox getMemoryDumpCheckBox() {
		return memoryDumpCheckBox;
	}

	public CheckBox getHaltCPUCheckBox() {
		return haltCPUCheckBox;
	}

	public ScreenViewController getScreenViewController() {
		return screenViewController;
	}

	public TextField getPathInputText() {
		return pathInputText;
	}

 	public ControlPanelValidator getValidator() {
		return validator;
	}
 	
	// Setter

	/**
 	 * Übergibt die controlPanelView Referenz an dieses Objekt (den ControlPanelController).
 	 * @param controlPanelView
 	 */
	public void setControlPanelView(ControlPanelView controlPanelView) {
		this.controlPanelView = controlPanelView;
	}

	public void setScreenViewController(ScreenViewController screenViewController) {
		this.screenViewController = screenViewController;
	}
}
