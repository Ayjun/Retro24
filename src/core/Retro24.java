package core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import core.CPU.CPU;
import core.IO.IOChip;
import core.graphics.GraphicChip;

/**
 * Repräsentation des emulierten Retro24 Systems und seinen Komponenten
 * @author Eric Schneider
 */
public class Retro24 {
	
	public static final int MEMORY_START = 0x0000;
	public static final int MEMORY_END = 0xFFFF;
	public static final int PROGRAMM_MEMORYSTART = 0x0100;
	public static final int TICK_ADDRESS = 0x0010;
	public static final int TOCK_ADDRESS = 0x0011;
	public static final String SUPPORTED_FILE_EXTENSION = ".bin";

	private byte[] memory;
	private CPU cpu;
	private GraphicChip graphicChip;
	private IOChip ioChip;

	/**
	 * Initialisert das System und alle Komponenten
	 */
	public void initialize() {
		memory = new byte[0x10000];
		// IO-Page initialisieren:
		for (int i = 0x0000; i <= 0x00FF; i++) {
			memory[i] = 0x00;
		}
		// Programmspeicher initialisieren:
		for (int i = 0x0100; i <= 0xDFFF; i++) {
			memory[i] = (byte)0xFF;
		}


		cpu = new CPU(this);
		cpu.initCPU();
		
		graphicChip = new GraphicChip(this);
		graphicChip.init();
		
		ioChip = new IOChip(this);
	}

    /**
     * Liest ein Byte aus dem Speicher an der gegebenen Adresse.
     * @overload Fuer addressen in short Form
     * @param address Die Adresse, von der gelesen werden soll.
     * @return Das Byte an der angegebenen Speicheradresse.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt.
     */
    public byte readMemory(short address) {
    	int uAddress = address & 0xFFFF;
        if (uAddress < 0 || uAddress >= memory.length) {
            throw new IllegalArgumentException("Adresse außerhalb des Speicherbereichs: " + uAddress);
        }
        return memory[uAddress];
    }
    
    /**
     * Liest ein Byte aus dem Speicher an der gegebenen Adresse.
     * @overload Fuer addressen in int Form
     * @param address Die Adresse, von der gelesen werden soll.
     * @return Das Byte an der angegebenen Speicheradresse.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt.
     */
    public byte readMemory(int address) {
        if (address < 0 || address >= memory.length) {
            throw new IllegalArgumentException("Adresse außerhalb des Speicherbereichs: " + address);
        }
        int uAddress = address & 0xFFFF; // Bitmaskierung für Konsistenz
        return memory[uAddress];
    }
    
    /**
     * Liest Daten aus dem Speicher von Startaddresse bis Endaddresse und gibt sie als byte Array zurück.
     * @overload Für Mehrzahl an byte Arrays in range
     * @param from Die Adresse, von der an gelesen werden soll.
     * @param to Die Adresse bis zu der gelesen werden soll (inklusive).
     * @return Das Byte an der angegebenen Speicheradresse.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt.
     */
    public byte[] readMemory(int from, int to) {
        // Prüfen, ob die Adressen im gültigen Bereich liegen
        if (from < 0 || from >= memory.length) {
            throw new IllegalArgumentException("Startadresse außerhalb des Speicherbereichs: " + from);
        }
        if (to < 0 || to >= memory.length) {
            throw new IllegalArgumentException("Endadresse außerhalb des Speicherbereichs: " + to);
        }
        if (from > to) {
            throw new IllegalArgumentException("Startadresse darf nicht größer als Endadresse sein: from=" + from + ", to=" + to);
        }

        // Arrays.copyOfRange erfordert eine exklusive Obergrenze
        return Arrays.copyOfRange(memory, from, to + 1);
    }

    
    /**
     * Schreibt übergebene Daten in den Speicher von Startadresse bis Endadresse.
     * @overload Für Mehrzahl an Bytes (byte Array)
     * @param from Die Adresse, von der an geschrieben werden soll.
     * @param to Die Adresse bis zu der geschrieben werden soll (inklusive).
     * @param data Das Array mit den Bytes, die in den Speicher geschrieben werden sollen.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt oder die Daten nicht passen.
     */
    public void writeMemory(int from, int to, byte[] data) {
        // Prüfen, ob die Adressen im gültigen Bereich liegen
        if (from < 0 || from >= memory.length) {
            throw new IllegalArgumentException("Startadresse außerhalb des Speicherbereichs: " + from);
        }
        if (to < 0 || to >= memory.length) {
            throw new IllegalArgumentException("Endadresse außerhalb des Speicherbereichs: " + to);
        }
        if (from > to) {
            throw new IllegalArgumentException("Startadresse darf nicht größer als Endadresse sein: from=" + from + ", to=" + to);
        }
        
        // Prüfen, ob die Daten ins angegebene Speicherintervall passen
        int length = to - from + 1;
        if (data.length != length) {
            throw new IllegalArgumentException("Datenlänge stimmt nicht mit Adressbereich überein: erwartet " + length + ", erhalten " + data.length);
        }

        // Daten in den Speicher schreiben
        System.arraycopy(data, 0, memory, from, length);
    }


    /**
     * Schreibt ein Byte in den Speicher an die gegebene Adresse.
     * @overload Fuer addressen in short Form
     * @param address Die Adresse, an die geschrieben werden soll.
     * @param value Das Byte, das gespeichert werden soll.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt.
     */
    public void writeMemory(short address, byte value) {
    	int uAddress = address & 0xFFFF;
        if (uAddress < 0 || uAddress >= memory.length) {
            throw new IllegalArgumentException("Adresse außerhalb des Speicherbereichs: " + uAddress);
        }
        memory[uAddress] = value;
    }

    /**
     * Schreibt ein Byte in den Speicher an die gegebene Adresse.
     * @overload Fuer addressen in int Form
     * @param address Die Adresse, an die geschrieben werden soll.
     * @param value Das Byte, das gespeichert werden soll.
     * @throws IllegalArgumentException Wenn die Adresse außerhalb des Speicherbereichs liegt.
     */
    public void writeMemory(int address, byte value) {
    	address = address & 0xFFFF;
        if (address < 0 || address >= memory.length) {
            throw new IllegalArgumentException("Adresse außerhalb des Speicherbereichs: " + address);
        }
        memory[address] = value;
    }

    /**
     * Lädt ein Programm von einem angegebenen Dateipfad in den Speicher des Retro24,
     * die erste Adresse ist hierbei 0x0100 (Programmstartadresse im Programmspeicher)
     * @param path
     * @throws IOException
     */
    public void loadProgramm(String path) {
    	byte[] programm = null;
    	BufferedInputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(path));
			programm = in.readAllBytes();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Datei kann nicht geoeffnet werden: " + path);
		}
    		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Fehler beim Lesen der Datei: " + path);
		}

    	loadProgramm(programm);
    }

    /**
     * Lädt ein Programm aus einem Bytearray in den Speicher des Retro24,
     * die erste Adresse ist hierbei 0x0100 (Programmstartadresse im Programmspeicher)
     * @param path
     */
    public void loadProgramm(byte[] programm) {
    	for (int i=0x0100, j=0; i<0x0100+programm.length; i++, j++) {
        	writeMemory(i, programm[j]);
        }
    }
    
    public void runNextInstruction() {
    	cpu.executeOpcode();
    }

    /**
     * @return die CPU der Retro24 Instanz
     */
    public CPU getCPU() {
    	return cpu;
    }
    
    /**
     * @return Grafikchip der Retro24 Instanz
     */
    public GraphicChip getGraphicChip() {
    	return graphicChip;
    }
    
    public IOChip getIOChip() {
    	return ioChip;
    }
}
