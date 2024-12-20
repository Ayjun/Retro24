package gui.controller.binder;

import gui.controller.ControlPanelController;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;

/**
 * Verwaltet alle Bindings des ControlPanels.
 * Trennt die Binding-Logik vom Controller.
 */
public class ControlPanelBinder {
    private final ControlPanelController controller;

    public ControlPanelBinder(ControlPanelController controller) {
        this.controller = controller;
    }

    /**
     * Initialisiert alle INTERNEN Bindings und Listener
     */
    public void initializeBindings() {
        bindCheckBoxProperties();
        bindVisibilityProperties();
        bindInputProperties();
        bindDisableProperties();
    }

    /**
     * Bindet die CheckBox-Properties
     */
    private void bindCheckBoxProperties() {
        controller.memoryDumpCheckBoxBP().bind(
            controller.getMemoryDumpCheckBox().selectedProperty());
        controller.instructionInfoCheckBoxBP().bind(
            controller.getInstructionInfoCheckBox().selectedProperty());
        controller.haltCPUCheckBoxBP().bind(
            controller.getHaltCPUCheckBox().selectedProperty());
    }

    /**
     * Bindet die Sichtbarkeits-Properties für memoryDump und instructionInfo
     */
    private void bindVisibilityProperties() {
        BooleanProperty memoryDumpBP = controller.memoryDumpCheckBoxBP();
        // Memory Dump Komponenten
        controller.getVonMemoryDumpInput().visibleProperty().bind(memoryDumpBP);
        controller.getVonMemoryDumpText().visibleProperty().bind(memoryDumpBP);
        controller.getBisMemoryDumpInput().visibleProperty().bind(memoryDumpBP);
        controller.getBisMemoryDumpText().visibleProperty().bind(memoryDumpBP);
        controller.getMemoryDumpListView().visibleProperty().bind(memoryDumpBP);

        // Instruction Info Komponenten
        BooleanProperty instructionInfoBP = controller.instructionInfoCheckBoxBP();
        controller.getInstructionInfoListView().visibleProperty().bind(instructionInfoBP);

        // CPU Halt Komponenten
        BooleanProperty haltCPUBP = controller.haltCPUCheckBoxBP();
        controller.getCpuStepButton().visibleProperty().bind(haltCPUBP);
        controller.getCpuStepButtonImage().visibleProperty().bind(haltCPUBP);
        controller.getEinrastenText().visibleProperty().bind(haltCPUBP);
    }

    /**
     * Bindet die Input-Field Properties
     */
    private void bindInputProperties() {
        controller.memoryDumpInputToSP().bind(
            controller.getBisMemoryDumpInput().textProperty());
        controller.memoryDumpInputFromSP().bind(
            controller.getVonMemoryDumpInput().textProperty());
        controller.pathInputTextSP().bindBidirectional(
            controller.getPathInputText().textProperty());
    }

    /**
     * Bindet die Disable-Properties für User-Input
     */
    private void bindDisableProperties() {
        BooleanProperty inputInactiveBP = controller.inputInactiveBP();
        controller.getHaltCPUCheckBox().disableProperty().bind(inputInactiveBP);
        controller.getInstructionInfoCheckBox().disableProperty().bind(inputInactiveBP);
        controller.getMemoryDumpCheckBox().disableProperty().bind(inputInactiveBP);
        controller.getVonMemoryDumpInput().disableProperty().bind(inputInactiveBP);
        controller.getBisMemoryDumpInput().disableProperty().bind(inputInactiveBP);
    }

    /**
     * Bindet Properties zu externen Komponenten (z.B. ScreenViewController)
     */
    public void bindExternalProperties() {
        if (controller.getScreenViewController() != null) {
            controller.inputInactiveBP().bind(
                controller.getScreenViewController().systemRunningBP());
            
            // Logs an ListViews binden
            controller.getInstructionInfoListView().setItems(
                controller.getScreenViewController().getInstructionLogObservable());
            controller.getMemoryDumpListView().setItems(
                controller.getScreenViewController().getMemoryLogObservable());
        }
    }
    

    /**
     * Setzt Listener für externe Abhängigkeiten.
     */
    public void initializeExternalListeners() {
    	setupAutoScrollListeners();
        setupSystemRunningListener();
    }

    /**
     * Listener für systemRunningBP des ScreenViewController.
     * Entfernt das Binding von inputInactiveBP wenn systemRunningBP false wird,
     * damit beim erneuten Start das inputInactiveBP.set() funktioniert.
     */
    private void setupSystemRunningListener() {
        if (controller.getScreenViewController() != null) {
            controller.getScreenViewController().systemRunningBP().addListener(
                (obs, oldValue, newValue) -> {
                    if (!newValue) {
                        controller.inputInactiveBP().unbind();
                    }
                }
            );
        }
    }

    /**
     * Listener für automatisches Scrollen der ListViews bei neuen Einträgen.
     */
    private void setupAutoScrollListeners() {
        controller.getInstructionInfoListView().getItems().addListener(
            (ListChangeListener<String>) change -> controller.tailInstructionListView()
        );

        controller.getMemoryDumpListView().getItems().addListener(
            (ListChangeListener<String>) change -> controller.tailMemoryDumpListView()
        );
    }
}