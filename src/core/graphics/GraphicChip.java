package core.graphics;

import java.util.Arrays;
import java.util.Random;

import core.Retro24;

public class GraphicChip {
	private final Retro24 retro24;
	private final int videoMemStartAddress;
	private final int videoMemEndAddress;
	
	public GraphicChip(Retro24 retro24, int videoMemStartAddress, int videoMemEndAddress) {
		this.videoMemStartAddress = videoMemStartAddress;
		this.videoMemEndAddress = videoMemEndAddress;
		this.retro24 = retro24;
	}
	
	public byte[] getVideoMemory() {
    	return retro24.readMemory(videoMemStartAddress, videoMemEndAddress);
    }
	
	/**
	 * Lädt den Startbildschirm
	 */
	public void init() {
		loadRandomTestImage();
		//retro24.writeMemory(videoMemStartAddress, videoMemEndAddress, generateRetro24Screen(64, 64, 0, 0x1000));
		setUpdateFlag(true);
	}
	
	/**
	 * Schreibt ein zufälliges Testbild in den Videospeicher
	 */
	public void loadRandomTestImage() {
		Random r = new Random();
		for (int i = 0xE000; i < 0xFFFF; i++) {
			retro24.writeMemory(i, (byte) r.nextInt());
		}
	}
	
	public boolean getUpdateFlag() {
		return retro24.readMemory(0x000A) == 0x01;
	}
	
	public void setUpdateFlag(boolean flag) {
		byte byteFlag = (byte) (flag ? 1 : 0);
		retro24.writeMemory(0x000A, byteFlag);
	}
	
	
	/**
	 * Schreibt Retro24 in den V
	 * @param width
	 * @param height
	 * @param darkLightStartAddress
	 * @param colorStartAddress
	 * @return
	 */
	public byte[] generateRetro24Screen(int width, int height, int darkLightStartAddress, int colorStartAddress) {
	    // Gesamtgröße des Speichers (Breite x Höhe des Screens)
	    int totalMemorySize = width * height;

	    // Initialisiere das Array für den Bildschirm
	    byte[] memory = new byte[totalMemorySize * 2]; // 2x Speicher, da wir sowohl Helligkeit als auch Farbe speichern

	    // Startposition für den Text im Grid
	    int startX = 5; // X-Position des Anfangs des Textes
	    int startY = 5; // Y-Position des Anfangs des Textes

	    // Die Buchstaben "RETRO24" als Pixel-Muster
	    String[] retro24Pattern = {
	        "1110111 1001111 1111001 1111110", // "R"
	        "1111111 1000000 1111111",        // "E"
	        "1111111 1001111 1001111",        // "T"
	        "1111111 1000001 1111111",        // "R"
	        "1110111 1001111 1111001",        // "O"
	        "1111001 1000111 1111111",        // "2"
	        "1111111 1111001 1111111"         // "4"
	    };

	    // Höhe und Breite der Buchstaben (Anpassung notwendig)
	    int charHeight = 7; // Höhe eines Buchstabens
	    int charWidth = 5;  // Breite eines Buchstabens

	    // Zeichne jeden Buchstaben in das Speicher-Array
	    for (int lineIndex = 0; lineIndex < retro24Pattern.length; lineIndex++) {
	        String pattern = retro24Pattern[lineIndex];

	        for (int i = 0; i < charHeight; i++) {
	            for (int j = 0; j < charWidth; j++) {
	                int pixelX = startX + (lineIndex * (charWidth + 1)) + j;
	                int pixelY = startY + i;

	                int pixelIndex = pixelY * width + pixelX;
	                int darkLightIndex = darkLightStartAddress + pixelIndex;
	                int colorIndex = colorStartAddress + pixelIndex;

	                if (pattern.charAt(i * charWidth + j) == '1') {
	                    memory[darkLightIndex] = 1; // Hell
	                    memory[colorIndex] = 1; // Farbiger Pixel
	                }
	            }
	        }
	    }

	    return memory;
	}
}
