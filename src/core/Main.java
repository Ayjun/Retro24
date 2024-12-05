package core;

import java.util.Scanner;

import core.CPU.CPU;
import core.CPU.Instruction;
import static util.StringUtil.*;

/**
 * Main Klasse, existiert aktuell hauptsächlich zu Test- und Debugzwecken.
 * @author Eric Schneider
 */
public class Main {
    public static void main(String[] args) {
        // Erstelle eine Instanz des Retro24-Systems
        Retro24 retro24 = new Retro24();

        // Initialisiere das Retro24-System
        retro24.initialize();

        // Programm laden:
        retro24.loadProgramm("/home/eric/pCloudDrive/Studium/Retrocomputing/RETRO24/Programme/Mattis/MS2a.bin");

        // Erstelle eine Instanz der CPU
        CPU cpu = retro24.getCPU();
        dumpMemory(retro24, (short)0x0100, (short)0x01FF);
        run(cpu, 50);
        dumpMemory(retro24, (short) 0x0100, (short) 0x01FF);

    }

    /**
     * Lässt die CPU endlos laufen (bis zum Haltebefehl)
     * @param cpu die CPU welche laufen soll
     * @param hz die CPU Frequenz (Takt)
     */
    private static void run(CPU cpu, int hz) {
    	long instructionCount = 1;
    	long startTime = System.nanoTime();
    	int sleeptime = 1000/hz;
    	printRegisterState(cpu);   // Zeige Register nach der Ausführung
    	cpu.executeOpcode(); // Erste Instruktion ausführen
        // Starten
    	while (!cpu.isHalted()){
        	printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
            printRegisterState(cpu);   // Zeige Register nach der Ausführung
        	cpu.executeOpcode();
        	instructionCount++;
        	try {
    			Thread.sleep(sleeptime);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.err.println("Sleep Timer unterbrochen!");
    		}
    	}
        printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
        printRegisterState(cpu);   // Zeige Register nach der Ausführung

        // Record the end time
        long endTime = System.nanoTime();

        // Calculate and print the elapsed time in milliseconds
        long elapsedTimeMillis = (endTime - startTime) / 1_000_000;
        System.out.println("Elapsed Time: " + elapsedTimeMillis + " ms");
        System.out.println("Instruction counter: " + instructionCount);
    }

    /**
     * Lässt die CPU endlos laufen (bis zum Haltebefehl),
     * wobei jede Instruktion einzeln ausgeführt und dann auf Eingabe gewartet wird
     * um den Verlauf nachvollziehen zu können (debugging).
     * @param cpu die CPU welche laufen soll
     */
    private static void debug(CPU cpu) {
    	Scanner scan = new Scanner(System.in);
    	printRegisterState(cpu);   // Zeige Register nach der Ausführung
    	cpu.executeOpcode(); // Erste Instruktion ausführen
        // Starten
    	while (!cpu.isHalted()){
    		scan.nextLine();
        	printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
            printRegisterState(cpu);   // Zeige Register nach der Ausführung
        	cpu.executeOpcode();
    	}
        printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
        printRegisterState(cpu);   // Zeige Register nach der Ausführung

        scan.close();
    }

    /**
     * Gibt die Registerinhalte der cpu auf der Konsole aus
     * @param cpu
     */
    private static int i=1;
    public static void printRegisterState(CPU cpu) {
        System.out.println("Registerinhalt:");
        System.out.println("R0: " + String.format("0x%02X", cpu.getR0()));
        System.out.println("R1: " + String.format("0x%02X", cpu.getR1()));
        System.out.println("R2: " + String.format("0x%02X", cpu.getR2()));
        System.out.println("R3: " + String.format("0x%02X", cpu.getR3()));
        System.out.println("IC: " + String.format("0x%04X", cpu.getIC()));
        System.out.println("AR: " + String.format("0x%04X", cpu.getAR()));
        System.out.println("########################"+i);
    }

    /**
     * Gibt die Details des zuletzt ausgeführten Opcodes der cpu auf der Konsole aus
     * @param cpu
     */
    public static void printLastOpcode(CPU cpu) {
    	Instruction lastInstruction = cpu.getLastInstruction();
    	System.out.println("Letzter Opcode:");
    	System.out.println("Opcode:	" + String.format("0x%02X", cpu.getLastInstruction().getOpcode()));
    	System.out.print  ("Args:	");

    	if (cpu.getLastInstruction().getLength() == 1) {
    		System.out.println("keine");
    	}
    	else {
    		System.out.println(byteArrayToString(lastInstruction.getArgs()));
    	}

    	System.out.println("Len:	" + lastInstruction.getLength() + " Byte");
    	System.out.println(lastInstruction.getAssemblerCode() + " " + byteArrayToString(lastInstruction.getArgs()));
    	System.out.println("------------------------");
    	i+=1;
    }

    /**
     *  Methode, um einen bestimmten Bereich des Speichers auszugeben
     * @param retro24
     * @param vidmemstart Startspeicheradresse
     * @param vidmemend Endpeicheradresse
     */
    public static void dumpMemory(Retro24 retro24, int vidmemstart, int vidmemend) {
        // Umwandlung von short zu unsigned int durch Bitmaskierung
        int uFrom = vidmemstart & 0xFFFF; // Maske für die unteren 16 Bits (von 0 bis 65535)
        int uTo = vidmemend & 0xFFFF;

        // Überprüfen, ob die Bereichsgrenzen sinnvoll sind
        if (uFrom > uTo || uFrom < 0x0000 || uTo > 0xFFFF) {
            System.out.println("MemoryDump: Ungültiger Bereich!");
            return;
        }

        System.out.println("Speicherauszug von " + String.format("0x%04X", vidmemstart) + " bis " + String.format("0x%04X", vidmemend) + ":");

        // Formatierte Ausgabe des Speicherinhalts in 16er-Blöcken
        for (int address = uFrom; address <= uTo; address += 16) {
            System.out.print(String.format("0x%04X: ", address));

            // Zeige 16 Bytes pro Zeile an, oder bis zum Ende des Bereichs
            for (int offset = 0; offset < 16 && (address + offset) <= uTo; offset++) {
                // Hole den Wert aus dem Speicher
                byte value = retro24.readMemory((short)(address + offset));
                System.out.print(String.format("%02X ", value));
            }
            System.out.println();
        }
    }


}