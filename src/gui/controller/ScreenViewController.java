package gui.controller;

import common.config.InstructionInfoConfig;
import common.config.MemoryDumpConfig;
import common.util.debug.log.InstructionDumper;
import common.util.debug.log.InstructionLogger;
import common.util.debug.log.MemoryDumpLogger;
import common.util.debug.log.MemoryDumper;
import core.Retro24;
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
import javafx.util.Duration;

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
	private final ControlPanelController controlPanelController;
	private final ScreenView screenView;
	private final String programPath;
	
	
	private MemoryDumpLogger memoryLogger;
	private InstructionLogger instructionLogger;
	
	private Timeline logTransfer;

	// Boolean Properties:
	private BooleanProperty systemRunningBP = new SimpleBooleanProperty(true);
	private BooleanProperty cpuPausedBP = new SimpleBooleanProperty(false);
	
	ObservableList<String> memoryLogObs = FXCollections.observableArrayList();
	ObservableList<String> instructionLogObs = FXCollections.observableArrayList();
	
	/**
	 * Konstruktor des ScreenViewController
	 * @param controlPanelController der aufrufende controlPanelController
	 * @param programPath der Pfad des auszuführenden Programmes
	 */
	public ScreenViewController(ControlPanelController controlPanelController, String programPath,
		MemoryDumpConfig memoryDumpConfig, InstructionInfoConfig instructionInfoConfig) {
		this.controlPanelController = controlPanelController;
		this.screenView = new ScreenView();
		this.retro24 = new Retro24();
		retro24.initialize();
		this.programPath = programPath;
		this.memoryLogger = new MemoryDumpLogger(new MemoryDumper(retro24, memoryDumpConfig));
		this.instructionLogger = new InstructionLogger(new InstructionDumper(retro24, instructionInfoConfig));
		updateView();
	}
	
	/**
	 * Methode zum Erledigen von Dingen vor dem eigentlichen Systemstart (im ScreenViewController).
	 */
	private void beforeRunScreenViewController() {
		// Title Screen zurücksetzen:
        retro24.getGraphicChip().resetVidMem();
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
		if (retro24.getGraphicChip().getUpdateFlag()) {
			screenView.updateScreen(retro24.getGraphicChip().getVideoMemory());
			retro24.getGraphicChip().setUpdateFlag(false);
		}
    }
	
	
	
	/**
	 * Wird nach dem Systemstart ausgeführt
	 */
	private void afterRun() {
		systemRunningBP.set(false);
		retro24.getCPU().setHalt(true);
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
		    while (!retro24.getCPU().isHalted()) {
		    	if (cpuPausedBP.get()) {
		    		Thread.sleep(100);
		    		continue;
		    	}
		        stepCPU();
		    }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void stepCPU() {
		if (retro24.getCPU().isHalted()) {
			return;
		}
	    final long targetCycleDurationNanos = 1_000_000_000 / CPUFREQUENCY ; // Zeit eines Taktes in Nanosekunden

		long cycleStartTime = System.nanoTime(); // Startzeit des Zyklus

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
	
	/**
	 * Updated alle Logs
	 */
	private void updateLogs() {
		memoryLogger.log();
	    instructionLogger.log();
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
		memoryLogger.drainTo(memoryLogObs);
	}
	
	/**
	 * Übermittelt das instructionLog an sein observable Gegenstück, welches an die ListView gebunden ist,
	 * somit erscheinen die Einträge in der ListView Anzeige.
	 */
	public void drainInstructionLog() {
		if (!controlPanelController.instructionInfoCheckBoxBP().get()) return;
		instructionLogger.drainTo(instructionLogObs);
	}
	
	public GraphicChip getGraphicChip() {
		return retro24.getGraphicChip();
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
