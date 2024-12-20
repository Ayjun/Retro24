package gui.controller.handler;

import static common.util.StringUtil.hexStringToInt;
import static gui.util.ScrollBarChecker.isScrollBar;

import common.config.InstructionInfoConfig;
import common.config.MemoryDumpConfig;
import gui.controller.ControlPanelController;
import gui.controller.ScreenViewController;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Eventhandler für das ControlPanel, die Logik für Events im ControlPanel findet hier Platz.
 */
public class ControlPanelEventHandler {
	
	private final ControlPanelController controller;
	private CPUStepHandler cpuStepHandler;
	
	/**
	 * Erzeuge einen neuen ControlPanelEventHandler
	 * @param controlPanelController Referenz auf den Controller
	 */
	public ControlPanelEventHandler(ControlPanelController controlPanelController) {
		this.controller = controlPanelController;
	}

	public void initializeHandlers() {
		initializeCpuStepButtonHandler();
		initializeListViewHandlers(); 
	}
	
	private void initializeListViewHandlers() {
		initializeMemoryDumpListView();
		initializeInstructionInfoListView();
	}

	private void initializeMemoryDumpListView() {
		configureListView(controller.getMemoryDumpListView());
	}
	
	private void initializeInstructionInfoListView() {
		configureListView(controller.getInstructionInfoListView());
	}
	
	/**
	 * Stellt eine ListView<String> so ein, dass sie bei Rechtsklick
	 * den Inhalt ins Clipboard kopiert und ansonten nur auf der Scrollbar
	 * ansprechbar ist.
	 * @param listView
	 */
	private void configureListView(ListView<String> listView) {
	    listView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
	        // Wenn Rechtsklick, dann komplette Liste kopieren
	        if (event.getButton() == MouseButton.SECONDARY) {
	            String allItems = String.join(System.lineSeparator(), listView.getItems());
	            final ClipboardContent content = new ClipboardContent();
	            content.putString(allItems);
	            Clipboard.getSystemClipboard().setContent(content);
	            event.consume();
	            return;
	        }
	        
	        // Sonst nur Scrollbar-Klicks erlauben
	        if (!isScrollBar(event.getTarget())) {
	            event.consume();
	        }
	    });
	}

	private void initializeCpuStepButtonHandler() {
		// CPU Step Button Handler:
	    controller.getCpuStepButton().setOnMouseClicked((event) -> {
	    	// Wenn die CPU aktuell pausiert ist und Rechtsklick:
	    	if (event.getButton() == MouseButton.SECONDARY && controller.cpuPausedBP().get()) {
	    		controller.cpuPausedBP().set(false);
	    	}
	    	// Wenn die CPU aktuell nicht pausiert ist und Rechtsklick:
	    	else if (event.getButton() == MouseButton.SECONDARY && !controller.cpuPausedBP().get()) {
	    		controller.cpuPausedBP().set(true);
	    	}
	    	// Wenn die CPU pausiert ist und Linksklick:
	    	else if (event.getButton() == MouseButton.PRIMARY && controller.cpuPausedBP().get()) {
	    		if(cpuStepHandler != null) {
	    			cpuStepHandler.handle();
	    		}
	    	}
	    	// Wenn die CPU nicht pausiert ist und Linksklick:
	    	else if (event.getButton() == MouseButton.PRIMARY && !controller.cpuPausedBP().get()) {
	    		controller.cpuPausedBP().set(true);
	    	}
	    });
	}
	
	public void setCpuStepHandler(CPUStepHandler cpuStepHandler) {
		this.cpuStepHandler = cpuStepHandler;
	}
	
	// Logik der Buttons Felder, etc:
	
	/**
	 * Behandelt den Click auf den Start Button im Retro24 ControlPanel
	 */ 
	public void handleStartButtonClick() {
		controller.clearListViews();
		closeOldScreens();
		
		// Vor Validierung input Möglichkeiten deaktivieren (s.o. langer Kommentar)
		if (controller.inputInactiveBP().isBound()) {
			controller.inputInactiveBP().unbind();
		}
		controller.inputInactiveBP().set(true);
		
		// Validieren der Eingaben
		if (!controller.getValidator().validateMemoryDump() || 
				!controller.getValidator().validateProgramPath()) {
			controller.inputInactiveBP().set(false);
			return;
		}
		
		// MemoryDumpConfig erstellen:
		MemoryDumpConfig memoryDumpConfig = makeMemoryDumpConfig();
		// InstructionInfoConfig erstellen:
		InstructionInfoConfig instructionInfoConfig = 
				new InstructionInfoConfig(
						controller.instructionInfoCheckBoxBP().get());
		
		// Neuen ScreenViewController (steuert Retro24 Instanz) anlegen:
		controller.setScreenViewController(
				new ScreenViewController(
				controller, 
				controller.pathInputTextSP().get(),
				memoryDumpConfig, 
				instructionInfoConfig));
		
		controller.runBeforeScreenViewStart();
		
		// Bildschirm des Retro24 anzeigen:
		controller.getScreenViewController().showScreen();
	}
	
	/**
	 * Behandelt den Click auf das Pfadeingabefeld im Retro24 ControlPanel
	 * Falls noch der default Wert eingetragen ist, wird das Feld geleert.
	 */
	public void handlePathInputTextClick() {
		if (!controller.pathInputTextSP().get().equals(
				ControlPanelController.PROGRAMM_PATH_INPUT_DEFAULT)) {
			return;
		}
		controller.pathInputTextSP().set("");
	}
	
	/**
	 * Behandelt den Click auf ... Button zum Programmpfad suchen
	 */
	public void handleLookForFileButtonClick() {
		String path = controller.openFileDialog();
		controller.pathInputTextSP().set(path);
	}
	
	/**
	 * Wenn es bereits einen screenViewController gab, schließe seine ScreenView
	 * und lösche die Referenz.
	 */
	private void closeOldScreens() {
		if (controller.getScreenViewController() == null) {
			return;
		}
		controller.getScreenViewController().closeScreen();
		controller.setScreenViewController(null);
	}
	
	/**
	 * Erstellt auf Basis der aktuellen Eingaben eine MemoryDumpConfig.
	 * @return die MemoryDumpConfig
	 */
	private MemoryDumpConfig makeMemoryDumpConfig() {
		int startAddress = hexStringToInt(controller.memoryDumpInputFromSP().get());
		int endAddress = hexStringToInt(controller.memoryDumpInputToSP().get());
		return new MemoryDumpConfig(controller.memoryDumpCheckBoxBP().get(), startAddress, endAddress);
	}
}