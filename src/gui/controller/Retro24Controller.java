package gui.controller;

import java.util.Random;

import core.Retro24;
import core.graphics.GraphicChip;
import gui.view.ScreenView;

public class Retro24Controller {
	private final Retro24 retro24;
	private final GraphicChip graphicChip;
	public ScreenView screenView;
	
	public Retro24Controller() {
		this.screenView = new ScreenView(64, 64, 0x0000, 0x1000);
		this.retro24 = new Retro24();
		retro24.initialize();
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
}
