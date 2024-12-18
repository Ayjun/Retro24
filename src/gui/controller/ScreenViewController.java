package gui.controller;

import static util.DebugUtil.*;
import static util.StringUtil.*;


import core.Retro24;
import core.CPU.CPU;
import core.graphics.GraphicChip;
import gui.view.ScreenView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.util.Duration;
import util.Log;

/**
 * Schnittstelle zwischen Retro24 System und der View, steuert das Model und die View.
 * @author Eric Schneider
 */
public class ScreenViewController {
	/**
	 * Die CPU-Frequenz in Hz
	 */
	public static final int CPUFREQUENCY = 1000;
	
	private final Retro24 retro24;
	private final GraphicChip graphicChip;
	private final CPU cpu;
	private ScreenView screenView;
	private String programPath;
	private ControlPanelController controlPanelController;
	
	private final boolean dumpMemory;
	private int dumpMemoryFrom;
	private int dumpMemoryTo;
	private Log memoryLog;
	
	private final boolean instructionInfoLog;
	private Log instructionLog;
	
	
	private int currentInstruction = 0;
	
	private Timeline logTransfer;

	private BooleanProperty systemRunning;
	
	BooleanProperty stepButtonPressed = new SimpleBooleanProperty(true);
	BooleanProperty cpuPaused = new SimpleBooleanProperty(false);
	
	public ScreenViewController(ControlPanelController controlPanelController) {
		systemRunning = new SimpleBooleanProperty(true);
		this.controlPanelController = controlPanelController;
		this.screenView = new ScreenView(GraphicChip.PIXELWIDTH, GraphicChip.PIXELHEIGHT, 0x0000, 0x1000);
		this.retro24 = new Retro24();
		retro24.initialize();
		this.cpu = retro24.getCPU();
		this.graphicChip = retro24.getGraphicChip();
		this.instructionInfoLog = controlPanelController.isInstructionInfoSelected();
		this.dumpMemory = controlPanelController.isDumpMemorySelected();
		initializeLogs();
		updateView();
	}
	
	/**
	 * Methode zum Erledigen von Dingen vor dem eigentlichen Systemstart (im ScreenViewController).
	 */
	private void beforeRunScreenViewController() {
		// Title Screen zurücksetzen:
        graphicChip.resetVidMem();
        setBindings();
        updateView();
	}
	
	private void setBindings() {
		try {
	        cpuPaused.bind(controlPanelController.cpuPaused());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	
	private void initializeLogs() {
		if (dumpMemory) {
			this.memoryLog = new Log();
			this.memoryLogObs = FXCollections.observableArrayList();
		}
		if (instructionInfoLog) {
			this.instructionLog = new Log();
			this.instructionLogObs = FXCollections.observableArrayList();
		}
	}
	

	
	/**
	 * Setzt die Variablen für den Memory Dump 
	 * @param dumpFrom
	 * @param dumpTo
	 */
	public void setDumpMemory(int dumpFrom, int dumpTo) {
		if (!dumpMemory) {
			return;
		}
		this.dumpMemoryFrom = dumpFrom;
		this.dumpMemoryTo = dumpTo;
	}
    
	public void updateView() {
		// Videoupdate Flag prüfen
		if (graphicChip.getUpdateFlag()) {
			screenView.updateScreen(graphicChip.getVideoMemory());
			graphicChip.setUpdateFlag(false);
		}
    }
	
	
	
	/**
	 * Wird nach dem Systemstart ausgeführt
	 */
	private void afterRun() {
		systemRunning.set(false);
		this.cpu.setHalt(true);
		Platform.runLater(() -> {
			if (logTransfer != null) {
				logTransfer.stop();
				memoryLog.drainTo(memoryLogObs);
                instructionLog.drainTo(instructionLogObs);
			}
        });
		
	}
	
	/**
	 * Hauptschleife in der das System läuft
	 * @throws InterruptedException 
	 */
	private void mainLoop()  {
		
		try {
			stepCPU();
		    while (!cpu.isHalted()) {
		    	if (cpuPaused.get()) {
		    		continue;
		    	}
		        stepCPU();
		    }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public void stepCPU() throws InterruptedException {
		if (cpu.isHalted()) {
			return;
		}
	    final long targetCycleDurationNanos = 1_000_000_000 / CPUFREQUENCY ; // Zeit eines Taktes in Nanosekunden

		long cycleStartTime = System.nanoTime(); // Startzeit des Zyklus

        incrementInstructionCounter();
        retro24.runNextInstruction();
        updateView();
        updateLogs();

        long elapsedTime = System.nanoTime() - cycleStartTime; // Verstrichene Zeit in Nanosekunden
        long sleepTime = targetCycleDurationNanos - elapsedTime; // Verbleibende Zeit berechnen

        controlPanelController.tail();
        
        if (sleepTime > 0) {
            Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000)); // Schlafzeit in Millisekunden und Nanosekunden
            // System.out.println((System.nanoTime() - cycleStartTime) / 1000 + " mikrosekunden Zyklus");
        }
        
	}

	/**
	 * Startet das Retro24 System mit dem angegebenen Programm
	 * @throws InterruptedException 
	 */
	// TODO umpostionieren:
	ObservableList<String> memoryLogObs;
	ObservableList<String> instructionLogObs;
	public void runSystemNoDebug() {
	    // Wenn die Stage (retro24 screen) geschlossen wird, beende auch die CPU.
	    screenView.getStage().setOnCloseRequest((close) -> afterRun());

	    // Programm laden:
	    retro24.loadProgramm(programPath);

	    // Task für den Retro24-Hintergrundprozess erstellen
	    Task<Void> systemTask = new Task<>() {
	    	
	        @Override
	        protected Void call() throws InterruptedException  {
	        	beforeRunScreenViewController();
	        	
	            mainLoop();
	            
	            afterRun();
	            
	            return null;
	        }
	    };
	    
	    

	    // Task in einem neuen Thread starten
	    Thread systemThread = new Thread(systemTask);
	    systemThread.setDaemon(true); // Beende den Thread automatisch beim Schließen der App
	    systemThread.start();
	    logTransfer.play();
	}
	
	
	public ObservableList<String> getMemoryLogObservable() {
		return memoryLogObs;
	}
	
	public ObservableList<String> getInstructionLogObservable() {
		return instructionLogObs;
	}
	
	public void runSystem() {
		logTransfer = new Timeline(
	            new KeyFrame(
	                Duration.seconds(0.05),
	                event -> {
	                	// TODO Nur Update wenn es auch neuen Eintrag in Log gibt:
	                    Platform.runLater(() -> {
	                        memoryLog.drainTo(memoryLogObs);
	                        instructionLog.drainTo(instructionLogObs);
	                    });
	                }
	            )
	        );
		logTransfer.setCycleCount(Timeline.INDEFINITE);

		runSystemNoDebug();
	}
	
	/**
	 * Updated alle Logs
	 */
	private void updateLogs() {
		if (dumpMemory) {
	        updateMemoryLog();
	    }

	    if (instructionInfoLog) {
	        updateInstructionLog();
	    }
	}
	
	/** 
	 * Updated den Memory Log
	 */
	private void updateMemoryLog() {
		if (!dumpMemory) return;
		
		StringBuilder currentMemoryLogEntry = new StringBuilder();
		currentMemoryLogEntry.append("########### Instruction Number: " + currentInstruction + " ###########" + System.lineSeparator());
		currentMemoryLogEntry.append(dumpMemory(getRetro24(), dumpMemoryFrom, dumpMemoryTo) + System.lineSeparator());
		
		memoryLog.offer(currentMemoryLogEntry.toString());
	}
	
	/** 
	 * Updated den Instruction Log
	 */
	private void updateInstructionLog() {
		if (!instructionInfoLog) return;
		
		StringBuilder currentInstructionLogEntry = new StringBuilder();
		currentInstructionLogEntry.append("### Instruction Number: " + currentInstruction + System.lineSeparator());
		currentInstructionLogEntry.append("##  " + cpu.getLastInstruction() + " " + byteArrayToString(cpu.getLastInstruction().getArgs()) + System.lineSeparator());
		currentInstructionLogEntry.append(dumpLastCPUInstruction(getRetro24()) + System.lineSeparator());
		instructionLog.offer(currentInstructionLogEntry.toString());
	}
	
	/**
	 * Öffnet das Fenster für die Anzeige des Bildschirms in der View
	 */
	public void showScreen() {
		screenView.showRetro24Screen(this);
	}
	
	
	public void closeScreen() {
		// Falls CPU noch nicht angehalten, nun anhalten:
		this.retro24.getCPU().setHalt(true);
		
		// Bildschirm schliessen:
		if (this.screenView.getStage() != null) {
			this.screenView.getStage().close();
		}
	}
	
	public void stopSystem() {
		this.retro24.getCPU().setHalt(true);
	}
	
	public void setProgramPath(String path) {
		this.programPath = path;
	}
	
	public void setControlPanelController(ControlPanelController controlPanelController) {
		this.controlPanelController = controlPanelController;
	}
	
	public Retro24 getRetro24() {
		return this.retro24;
	}

	public Node getCanvas() {
		// TODO Auto-generated method stub
		return screenView.getCanvas();
	}
	
	private synchronized void incrementInstructionCounter() {
	    currentInstruction += 1;
	}
	
	public BooleanProperty isSystemRunningProperty() {
		return this.systemRunning;
	}
	
	/**
	 * Gibt die Logs in die View Logs
	 */
	public void drainLogs() {
		memoryLog.drainTo(memoryLogObs);
        instructionLog.drainTo(instructionLogObs);
	}
}
