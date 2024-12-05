package gui.controller;

import java.util.Scanner;

import core.Main;
import core.Retro24;
import core.CPU.CPU;
import core.graphics.GraphicChip;
import gui.view.ScreenView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Retro24Controller {
	private final Retro24 retro24;
	private final GraphicChip graphicChip;
	private final CPU cpu;
	public ScreenView screenView;
	
	public Retro24Controller() {
		this.screenView = new ScreenView(64, 64, 0x0000, 0x1000);
		this.retro24 = new Retro24();
		retro24.initialize();
		this.cpu = retro24.getCPU();
		this.graphicChip = retro24.getGraphicChip();
		updateView();
	}
	
	public void updateView() {
		// Videoupdate Flag prüfen
		if (graphicChip.getUpdateFlag()) {
			screenView.updateScreen(graphicChip.getVideoMemory());
			graphicChip.setUpdateFlag(false);
		}
    }
	
	/**
	 * Testetmethode für die Kommunikation zwischen model und view,
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
	

	
	public void runSystem() {
		//TESTPROGRAMM:
		retro24.loadProgramm("/home/eric/Downloads/output (36).bin");
		
		// DEBUGGING!
		Scanner scan = new Scanner(System.in);
		// ENDDEBUGGING
		
		Thread systemThread = new Thread(() -> {
			graphicChip.resetVidMem();
			updateView();
			
			Main.dumpMemory(retro24, 0x0100, 0x01FF);
			while(!cpu.isHalted()) {
				retro24.runNextInstruction();
				updateView();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Main.printLastOpcode(cpu);
				Main.printRegisterState(cpu);
				
				//scan.nextLine();
			}
			Main.dumpMemory(retro24, 0x0100, 0x01FF);
		});
		
		systemThread.start();
		
	}
}
