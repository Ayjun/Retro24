package core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import core.CPU.CPU;

public class Retro24 {
	
	private byte[] memory;
	private CPU cpu;
	
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
	}

    /**
     * Liest ein Byte aus dem Speicher an der gegebenen Adresse.
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
    
    
    /**
     * @return die CPU der Retro24 Instanz
     */
    public CPU getCPU() {
    	return cpu;
    }
    
    
}
