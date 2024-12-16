package gui.controller;

import static util.DebugUtil.*;
import static util.StringUtil.*;

import org.junit.internal.runners.statements.RunAfters;

import core.Retro24;
import core.CPU.CPU;
import core.graphics.GraphicChip;
import gui.view.ScreenView;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
	public static final int CPUFREQUENCY = 100;
	
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
	
	private final boolean cpuStepMode;
	
	private int currentInstruction = 0;
	
	private AnimationTimer guiUpdater;
	
	private BooleanProperty systemRunning;
	
	public ScreenViewController(ControlPanelController controlPanelController) {
		systemRunning = new SimpleBooleanProperty(true);
		this.controlPanelController = controlPanelController;
		this.screenView = new ScreenView(GraphicChip.PIXELWIDTH, GraphicChip.PIXELHEIGHT, 0x0000, 0x1000);
		this.retro24 = new Retro24();
		retro24.initialize();
		this.cpu = retro24.getCPU();
		this.graphicChip = retro24.getGraphicChip();
		this.cpuStepMode = controlPanelController.isCPUHaltenSelected();
		this.instructionInfoLog = controlPanelController.isInstructionInfoSelected();
		this.dumpMemory = controlPanelController.isDumpMemorySelected();
		initializeLogs();
		updateView();
	}
	
	private void initializeLogs() {
		if (dumpMemory) {
			this.memoryLog = new Log();
		}
		if (instructionInfoLog) {
			this.instructionLog = new Log();
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
	 * Testmethode für die Kommunikation zwischen model und view,
	 * lädt immer wechselnd Test- und Welcomescreen in den Grafikspeicher was dann im View angezeigt wird.
	 */
	int cycle = 0;
	public void test() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			graphicChip.setUpdateFlag(true);
	        if (cycle % 2 == 0) {
	        	graphicChip.loadRetro24WelcomeScreen();
	        } else {
	            graphicChip.loadRandomTestImage();
	        }
	        updateView();
	        cycle += 1;
	    }));
	    timeline.setCycleCount(5); // Wiederholt sich unendlich
	    timeline.play();
	}
	
	
	/**
	 * Wird vor dem Systemstart ausgeführt
	 */
	private void beforeRun() {
		// Title Screen zurücksetzen:
        graphicChip.resetVidMem();
        updateView();
	}
	
	/**
	 * Wird nach dem Systemstart ausgeführt
	 */
	private void afterRun() {
		systemRunning.set(false);
		this.cpu.setHalt(true);
		Platform.runLater(() -> {
			if (guiUpdater != null) {
				guiUpdater.stop();
			}
        	finalLogUpdate(); // Letztes Update der GUI nach Beendigung der Emulation
        });
		
	}
	
	/**
	 * Hauptschleife in der das System läuft
	 * @throws InterruptedException 
	 */
	private void mainLoop() throws InterruptedException {
	    final long targetCycleDurationNanos = 1_000_000_000 / CPUFREQUENCY ; // Zeit eines Taktes in Nanosekunden
	    while (!cpu.isHalted()) {
	        long cycleStartTime = System.nanoTime(); // Startzeit des Zyklus

	        incrementInstructionCounter();
	        retro24.runNextInstruction();
	        updateView();
	        updateLogs();

	        long elapsedTime = System.nanoTime() - cycleStartTime; // Verstrichene Zeit in Nanosekunden
	        long sleepTime = targetCycleDurationNanos - elapsedTime; // Verbleibende Zeit berechnen

	        if (sleepTime > 0) {
	            Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000)); // Schlafzeit in Millisekunden und Nanosekunden
	            // Debugging Info:
	            // System.out.println((System.nanoTime() - cycleStartTime) / 1000 + " mikrosekunden Zyklus");
	        } 
	    }
	}

	/**
	 * Startet das Retro24 System mit dem angegebenen Programm
	 * @throws InterruptedException 
	 */
	public void runSystemNoDebug() {
	    // Wenn die Stage (retro24 screen) geschlossen wird, beende auch die CPU.
	    screenView.getStage().setOnCloseRequest((close) -> afterRun());

	    // Programm laden:
	    retro24.loadProgramm(programPath);

	    // Task für den Retro24-Hintergrundprozess erstellen
	    Task<Void> systemTask = new Task<>() {
	        @Override
	        protected Void call() throws InterruptedException  {
	        	
	        	beforeRun();

	            mainLoop();
	            
	            afterRun();
	            
	            return null;
	        }
	    };
	    
        // GUI-Updates periodisch mit AnimationTimer
        guiUpdater = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
            	if (cpu.isHalted()) {
            		this.stop();
            	}
            	
                // Update alle 100 Millisekunden (~10 FPS)
                if (now - lastUpdate >= 100_000_000) { // 100 Millisekunden in Nanosekunden
                    Platform.runLater(() -> {
                        updateMemoryDumpTextArea();
                        updateInstructionLogTextArea();
                    });
                    lastUpdate = now;
                }
            }
        };

	    // Task in einem neuen Thread starten
	    Thread systemThread = new Thread(systemTask);
	    systemThread.setDaemon(true); // Beende den Thread automatisch beim Schließen der App
	    systemThread.start();
	    guiUpdater.start();
	}
	
	
	public void runSystem() {
		if (cpuStepMode) {
			runSystemDebug();
		}
		else {
			runSystemNoDebug();
		}
	}
	
	
	/**
	 * Startet das Retro24 System mit dem angegebenen Programm im Debug Modus (CPU halten)
	 */
	public void runSystemDebug() {
		
		BooleanProperty stepButtonPressed = new SimpleBooleanProperty();
		stepButtonPressed.bindBidirectional(controlPanelController.getCPUStepActivity());
		
	    // Wenn die Stage (retro24 screen) geschlossen wird, beende auch die CPU.
	    screenView.getStage().setOnCloseRequest((close) -> afterRun());

	    // Programm laden:
	    retro24.loadProgramm(programPath);

	    // Task für den Retro24-Hintergrundprozess erstellen
	    Task<Void> systemTask = new Task<>() {
	        @Override
	        protected Void call() throws Exception {
	      
	        	beforeRun();
	        	
	            // Mainloop, solange CPU nicht anhält:
	            while (!cpu.isHalted()) {

	            	stepButtonPressed.set(false);
	            	incrementInstructionCounter();
	                retro24.runNextInstruction();
	                updateView();
	                updateLogs();
	                
	                Platform.runLater(() -> {
                        updateMemoryDumpTextArea();
                        updateInstructionLogTextArea();
                    });
	                
	                // Wenn step nicht getriggert schleife ohne was zu tun wiederholen:
	            	while (!stepButtonPressed.get()) {
	            		Thread.sleep(100);
	            	}
	            	
	                Thread.sleep(10);
	            }
	            
	            afterRun();

	            return null;
	        }
	    };

	    // Task in einem neuen Thread starten
	    Thread systemThread = new Thread(systemTask);
	    systemThread.setDaemon(true); // Beende den Thread automatisch beim Schließen der App
	    systemThread.start();
	}

	/**
	 * Finales Update der Log-Anzeigen nach Halten der CPU.
	 */
	private void finalLogUpdate() {
		if (dumpMemory) {
			setMemoryDump();
		}
		if (instructionInfoLog) {
			setInstructionLog();
		}
        updateView();
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
		currentMemoryLogEntry.append(dumpMemory(getRetro24(), dumpMemoryFrom, dumpMemoryTo));
		
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
		currentInstructionLogEntry.append(dumpLastCPUInstruction(getRetro24()));
		instructionLog.offer(currentInstructionLogEntry.toString());
	}

	/**
	 * Live Updaten der Textarea in der der Memorydump angezeigt wird:
	 */
	public void updateMemoryDumpTextArea() {
	    if (!dumpMemory || memoryLog.peekLast() == null) return;
	    
	    controlPanelController.updateMemoryDumpTextArea(System.lineSeparator() + memoryLog.peekLast());
	}
	
	/**
	 * Live Updaten der Textarea in der CPU Instruktionen angezeigt werden:
	 */
	public void updateInstructionLogTextArea() {
		if (!instructionInfoLog || instructionLog.peekLast() == null) return;
		
	    controlPanelController.updateInstructionInfoTextArea(instructionLog.peekLast() + System.lineSeparator());
	}
	
	/**
	 * Setzen der memory dump Anzeige mit dem gespeicherten Log
	 * @param log
	 */
	public void setMemoryDump() {
		controlPanelController.setMemoryDumpTextArea(this.memoryLog.toString());
	}
	
	/**
	 * Setzen der Instruction Log Anzeige mit dem gespeicherten Log
	 * @param log
	 */
	public void setInstructionLog() {
		controlPanelController.setInstructionInfoTextArea(this.instructionLog.toString());
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
}
