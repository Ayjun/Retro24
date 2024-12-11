package gui.controller;

import static util.DebugUtil.*;

import core.Retro24;
import core.CPU.CPU;
import core.graphics.GraphicChip;
import gui.view.ScreenView;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Schnittstelle zwischen Retro24 System und der View, steuert das Model und die View.
 * @author Eric Schneider
 */
public class ScreenViewController {
	private final Retro24 retro24;
	private final GraphicChip graphicChip;
	private final CPU cpu;
	private ScreenView screenView;
	private String programPath;
	private ControlPanelController controlPanelController;
	
	private final boolean dumpMemory;
	private int dumpMemoryFrom;
	private int dumpMemoryTo;
	private StringBuilder memoryLog;
	
	private final boolean instructionInfoLog;
	private StringBuilder instructionLog;
	
	private final boolean cpuStepMode;
	
	private int currentInstruction = 0;
	
	public ScreenViewController(ControlPanelController controlPanelController) {
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
	
	public void initializeLogs() {
		if (dumpMemory) {
			this.memoryLog = new StringBuilder();
		}
		if (instructionInfoLog) {
			this.instructionLog = new StringBuilder();
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
	 * Startet das Retro24 System mit dem angegebenen Programm
	 */
	public void runSystem() {
	    // Wenn die Stage (retro24 screen) geschlossen wird, beende auch die CPU.
	    screenView.getStage().setOnCloseRequest((close) -> stopSystem());

	    // Programm laden:
	    retro24.loadProgramm(programPath);

	    // Task für den Retro24-Hintergrundprozess erstellen
	    Task<Void> systemTask = new Task<>() {
	        @Override
	        protected Void call() throws Exception {
	            // Title Screen zurücksetzen:
	            graphicChip.resetVidMem();
	            updateView();

	            // Mainloop, solange CPU nicht anhält:
	            while (!cpu.isHalted()) {
	            	incrementInstructionCounter();
	                retro24.runNextInstruction();
	                updateView();
	                updateLogs();
	                
	                // TODO ÄNDERN AUF ANDERE METHODE ZUR FREQUENZ SIMULATION
	                Thread.sleep(10);
	            }
	            
	         // Letztes Update der GUI nach Beendigung der Emulation
	            Platform.runLater(() -> {
	            	finalLogUpdate();
	            });
	            
	            return null;
	        }
	    };
	    
	    
	    
	    AnimationTimer guiUpdater; 
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
		memoryLog.append(System.lineSeparator());
		memoryLog.append("########### Instruction Number: " + currentInstruction + " ###########" + System.lineSeparator());
		memoryLog.append(dumpMemory(getRetro24(), dumpMemoryFrom, dumpMemoryTo));
	}
	
	/** 
	 * Updated den Instruction Log
	 */
	private void updateInstructionLog() {
		if (!instructionInfoLog) return;
		instructionLog.append(System.lineSeparator());
		instructionLog.append("### Instruction Number: " + currentInstruction + " ###" + System.lineSeparator());
		instructionLog.append(dumpLastCPUInstruction(getRetro24()));
	}

	/**
	 * Live Updaten der Textarea in der der Memorydump angezeigt wird:
	 */
	public void updateMemoryDumpTextArea() {
	    if (!dumpMemory) return;
	    StringBuilder currentDump = new StringBuilder();
	    
	    currentDump.append(System.lineSeparator());
	    currentDump.append("########### Instruction Number: " + currentInstruction + " ###########" + System.lineSeparator());
	    currentDump.append(dumpMemory(getRetro24(), dumpMemoryFrom, dumpMemoryTo));

	    controlPanelController.updateMemoryDumpTextArea(currentDump.toString());
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
		this.screenView.getStage().close();
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
}
