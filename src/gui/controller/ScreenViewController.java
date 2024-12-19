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
	private final ControlPanelController controlPanelController;
	private final ScreenView screenView;
	private final String programPath;
	
	private Log memoryLog = new Log();
	private Log instructionLog = new Log();
	
	private int currentInstruction = 0;
	
	private Timeline logTransfer;

	// Boolean Properties:
	private BooleanProperty systemRunningBP = new SimpleBooleanProperty(true);
	private BooleanProperty cpuPausedBP = new SimpleBooleanProperty(false);
	
	/**
	 * Konstruktor des ScreenViewController
	 * @param controlPanelController der aufrufende controlPanelController
	 * @param programPath der Pfad des auszuführenden Programmes
	 */
	public ScreenViewController(ControlPanelController controlPanelController, String programPath) {
		this.controlPanelController = controlPanelController;
		this.screenView = new ScreenView(GraphicChip.PIXELWIDTH, GraphicChip.PIXELHEIGHT, 0x0000, 0x1000);
		this.retro24 = new Retro24();
		retro24.initialize();
		this.cpu = retro24.getCPU();
		this.graphicChip = retro24.getGraphicChip();
		this.programPath = programPath;
		updateView();
	}
	
	/**
	 * Methode zum Erledigen von Dingen vor dem eigentlichen Systemstart (im ScreenViewController).
	 */
	private void beforeRunScreenViewController() {
		// Title Screen zurücksetzen:
        graphicChip.resetVidMem();
        bindExternalProperties();
        updateView();
	}
	
	private void bindExternalProperties() {
		try {
			cpuPausedBP().bindBidirectional(controlPanelController.cpuPausedBP());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
		systemRunningBP.set(false);
		this.cpu.setHalt(true);
		Platform.runLater(() -> {
			if (logTransfer != null) {
				logTransfer.stop();
				drainLogs();
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
		    	if (cpuPausedBP.get()) {
		    		continue;
		    	}
		        stepCPU();
		    }
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public void stepCPU() {
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
        
        if (sleepTime > 0) {
            try {
				Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
	}

	/**
	 * Startet das Retro24 System mit dem angegebenen Programm
	 * @throws InterruptedException 
	 */
	// TODO umpostionieren:
	ObservableList<String> memoryLogObs = FXCollections.observableArrayList();
	ObservableList<String> instructionLogObs = FXCollections.observableArrayList();
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
	
	public void runSystem() {
		logTransfer = new Timeline(
	            new KeyFrame(
	                Duration.seconds(0.05),
	                event -> {
	                	// TODO Nur Update wenn es auch neuen Eintrag in Log gibt:
	                    Platform.runLater(() -> {
	                        drainLogs();
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
	    updateMemoryLog();
	    updateInstructionLog();
	}
	
	/** 
	 * Updated den Memory Log
	 */
	private void updateMemoryLog() {
		
		if (!controlPanelController.memoryDumpCheckBoxBP().get()) return;
		
		StringBuilder currentMemoryLogEntry = new StringBuilder();
		currentMemoryLogEntry.append("########### Instruction Number: " + currentInstruction + " ###########" + System.lineSeparator());
		currentMemoryLogEntry.append(dumpMemory(retro24, controlPanelController.getMemoryDumpStartAddress(), controlPanelController.getMemoryDumpEndAddress()) + System.lineSeparator());
		
		memoryLog.offer(currentMemoryLogEntry.toString());
	}
	
	/** 
	 * Updated den Instruction Log
	 */
	private void updateInstructionLog() {
		if (!controlPanelController.instructionInfoCheckBoxBP().get()) return;
		
		StringBuilder currentInstructionLogEntry = new StringBuilder();
		currentInstructionLogEntry.append("### Instruction Number: " + currentInstruction + System.lineSeparator());
		currentInstructionLogEntry.append("##  " + cpu.getLastInstruction() + " " + byteArrayToString(cpu.getLastInstruction().getArgs()) + System.lineSeparator());
		currentInstructionLogEntry.append(dumpLastCPUInstruction(retro24) + System.lineSeparator());
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
	
	private synchronized void incrementInstructionCounter() {
	    currentInstruction += 1;
	}
	
	/**
	 * Übermittelt alle Logs an ihre observable Gegenstücke, welche an die ListViews gebunden sind,
	 * somit werden diese auch angezeigt.
	 */
	public void drainLogs() {
		drainMemoryLog();
		drainInstructionLog();
	}
	
	/**
	 * Übermittelt das memoryLog an sein observable Gegenstück, welches an die ListView gebunden ist,
	 * somit erscheinen die Einträge in der ListView Anzeige.
	 */
	public void drainMemoryLog() {
		if (!controlPanelController.memoryDumpCheckBoxBP().get()) return;
		memoryLog.drainTo(memoryLogObs);
	}
	
	/**
	 * Übermittelt das instructionLog an sein observable Gegenstück, welches an die ListView gebunden ist,
	 * somit erscheinen die Einträge in der ListView Anzeige.
	 */
	public void drainInstructionLog() {
		if (!controlPanelController.instructionInfoCheckBoxBP().get()) return;
		instructionLog.drainTo(instructionLogObs);
	}
	
	public ObservableList<String> getMemoryLogObservable() {
		return memoryLogObs;
	}
	
	public ObservableList<String> getInstructionLogObservable() {
		return instructionLogObs;
	}
	
	public BooleanProperty systemRunningBP() {
		return systemRunningBP;
	}
	
	public BooleanProperty cpuPausedBP() {
		return cpuPausedBP;
	}
}
