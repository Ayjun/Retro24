package gui.controller.handler;

import static gui.util.ScrollBarChecker.isScrollBar;

import gui.controller.ControlPanelController;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Eventhandler für das ControlPanel, die Logik für Events im ControlPanel findet hier Platz.
 */
public class ControlPanelEventHandler {
	
	private final ControlPanelController controlPanelController;
	private CPUStepHandler cpuStepHandler;
	
	/**
	 * Erzeuge einen neuen ControlPanelEventHandler
	 * @param controlPanelController Referenz auf den Controller
	 */
	public ControlPanelEventHandler(ControlPanelController controlPanelController) {
		this.controlPanelController = controlPanelController;
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
		// Nur auf Scrollbar reagieren:
		controlPanelController.getMemoryDumpListView().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
	        if (!isScrollBar(event.getTarget())) {
	            event.consume(); // Alle anderen Klicks blockieren
	        }
	    });
	}
	
	private void initializeInstructionInfoListView() {
		// Nur auf Scrollbar reagieren:
		controlPanelController.getInstructionInfoListView().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
	        if (!isScrollBar(event.getTarget())) {
	            event.consume(); // Alle anderen Klicks blockieren
	        }
	    });
	}

	private void initializeCpuStepButtonHandler() {
		// CPU Step Button Handler:
	    controlPanelController.getCpuStepButton().setOnMouseClicked((event) -> {
	    	// Wenn die CPU aktuell pausiert ist und Rechtsklick:
	    	if (event.getButton() == MouseButton.SECONDARY && controlPanelController.cpuPausedBP().get()) {
	    		controlPanelController.cpuPausedBP().set(false);
	    	}
	    	// Wenn die CPU aktuell nicht pausiert ist und Rechtsklick:
	    	else if (event.getButton() == MouseButton.SECONDARY && !controlPanelController.cpuPausedBP().get()) {
	    		controlPanelController.cpuPausedBP().set(true);
	    	}
	    	// Wenn die CPU pausiert ist und Linksklick:
	    	else if (event.getButton() == MouseButton.PRIMARY && controlPanelController.cpuPausedBP().get()) {
	    		if(cpuStepHandler != null) {
	    			cpuStepHandler.handle();
	    		}
	    	}
	    	// Wenn die CPU nicht pausiert ist und Linksklick:
	    	else if (event.getButton() == MouseButton.PRIMARY && !controlPanelController.cpuPausedBP().get()) {
	    		controlPanelController.cpuPausedBP().set(true);
	    	}
	    });
	}
	
	public void setCpuStepHandler(CPUStepHandler cpuStepHandler) {
		this.cpuStepHandler = cpuStepHandler;
	}
}