package core.graphics;

import static common.util.NumberUtil.*;

import java.util.Random;

import core.Retro24;

/**
 * Virtueller Grafikchip des Retro24 Systems
 * 
 * Der GraphicChip dient als Kapselung aller Grafikfunktionalitäten des Retro24 Objekts.
 * @author Eric Schneider
 */
public class GraphicChip {
	
	public static final int UPDATE_FLAG_ADDRESS = 0x000A; // Speicheradresse des Update Flags
	public static final int IO_PAGE_START = 0x0000;
	public static final int IO_PAGE_END = 0x00FF;
	public static final int VIDMEM_START = 0xE000; // Speicheradresse wo der Videospeicher beginnt
	public static final int VIDMEM_END = 0xFFFF; // Speicheradresse wo der Videospeicher endet
	public static final int VIDMEM_DARK_LIGHT_START = VIDMEM_START;
	public static final int VIDMEM_MONOCHROM_COLOR_START = 0xF000;
	public static final int PIXEL_WIDTH = 64; // Pixelweite des Bildes
	public static final int PIXEL_HEIGHT = 64; // Pixelhöhe des Bildes
	
	private final Retro24 retro24;
	
	public GraphicChip(Retro24 retro24) {
		this.retro24 = retro24;
	}
	
	/**
	 * Übersetzt einen Pixel Koordinate in die entsprecheden Speicheradressen des Retro24
	 * @param x horizontale Koordinate des Pixels
	 * @param y verikale Koordinate des Pixels
	 * @return die entsrechenden Speicheradressen
	 * index 0 = Speicheradresse für Pixel (x/y) hell/dunkel
	 * index 1 = Speicheradresse für Pixel (x/y) monochrom/farbig
	 */
	public int[] getPixelAddresses(int x, int y) {
		int darkLightAddress = calculate1DAddress(x, y, PIXEL_WIDTH, PIXEL_HEIGHT, VIDMEM_DARK_LIGHT_START);
		int monochromColorAddress = calculate1DAddress(x, y, PIXEL_WIDTH, PIXEL_HEIGHT, VIDMEM_MONOCHROM_COLOR_START);
		return new int[] {darkLightAddress, monochromColorAddress};
	}
	
	
	/**
	 * Setzt die zu einem Pixel gehörenden 2 Byte im Hauptspeicher
	 * @param x horizontale Koordinate des Pixels
	 * @param y verikale Koordinate des Pixels
	 * @param darkLight der Wert für die Helligkeit (0 = dunkel, 1 = hell)
	 * @param monochromColor der Wert für die Farbe (0 = monochrom, 1 = farbig)
	 * @overload für x, y als Einzelwerte
	 */
	public void setVidMemPixel(int x, int y, boolean darkLight, boolean monochromColor) {
		int[] addresses = getPixelAddresses(x, y);
		retro24.writeMemory(addresses[0], booleanToByte(darkLight));
		retro24.writeMemory(addresses[1], booleanToByte(monochromColor));
	}
	
	/**
	 * Setzt die zu einem Pixel gehörenden 2 Byte im Hauptspeicher
	 * @param x horizontale Koordinate des Pixels
	 * @param y verikale Koordinate des Pixels
	 * @param darkLight der Wert für die Helligkeit (0 = dunkel, 1 = hell)
	 * @param monochromColor der Wert für die Farbe (0 = monochrom, 1 = farbig)
	 * @overload für Koordinaten array
	 */
	public void setVidMemPixel(int[] xy, boolean darkLight, boolean monochromColor) {
		int[] addresses = getPixelAddresses(xy[0], xy[1]);
		retro24.writeMemory(addresses[0], booleanToByte(darkLight));
		retro24.writeMemory(addresses[1], booleanToByte(monochromColor));
	}
	
	/**
	 * Gibt den Videospeicher zurück
	 * @return den Videospeicher des Retro24 als Byte Array
	 */
	public byte[] getVideoMemory() {
    	return retro24.readMemory(VIDMEM_START, VIDMEM_END);
    }
	
	/**
	 * Schreibt einen Wert in den Videospeicher.
	 * 
	 * @param address die Speicheradresse (muss zwischen VIDMEMSTART und VIDMEMEND liegen)
	 * @param value der zu schreibende Wert
	 * @throws IllegalArgumentException wenn die Adresse außerhalb des Videospeichers liegt
	 */
	public void writeToVideoMemory(int address, byte value) {
	    if (address < VIDMEM_START || address > VIDMEM_END) {
	        throw new IllegalArgumentException(
	            String.format("Adresse 0x%04X liegt außerhalb des Videospeichers (0x%04X-0x%04X)",
	            address, VIDMEM_START, VIDMEM_END));
	    }
	    retro24.writeMemory(address, value);
	}
	
	/**
	 * Initialisiert die Grafik (lädt den Startbildschirm)
	 */
	public void init() {
		StartScreenLoader startScreenLoader = new StartScreenLoader(this);
		startScreenLoader.loadRetro24WelcomeScreen();
		setUpdateFlag(true);
	}
	
	/**
	 * Liest das Video Update Flag von der entsprechenden Speicheraddresse des Hauptspeichers des Retro24.
	 * @return ob Update Flag gesetzt (true) oder nicht (false).
	 */
	public boolean getUpdateFlag() {
		return retro24.readMemory(UPDATE_FLAG_ADDRESS) == 0x01;
	}
	
	/**
	 * Setzt das Video Update Flag in der entsprechenden Speicheradresse des Hauptspeichers des Retro24.
	 */
	public void setUpdateFlag(boolean flag) {
		byte byteFlag = booleanToByte(flag);
		retro24.writeMemory(UPDATE_FLAG_ADDRESS, byteFlag);
	}
	
	/**
	 * Setzt alle Werte im Videospeicher auf 0x00
	 */
	public void resetVidMem() {
		for (int i = VIDMEM_START; i < VIDMEM_END; i++) {
			retro24.writeMemory(i, (byte) 0x00);
		}
	}
}
