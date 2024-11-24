package core;

import java.util.Scanner;

import core.CPU.CPU;

public class Main {
    public static void main(String[] args) {
        // Erstelle eine Instanz des Retro24-Systems
        Retro24 retro24 = new Retro24();

        // Initialisiere das Retro24-System
        retro24.initialize();
        
        // Programm laden:
        retro24.loadProgramm("./Programme/addiereZahlen7bis77.bin");

        // Erstelle eine Instanz der CPU
        CPU cpu = retro24.getCPU();
        dumpMemory(retro24, (short)0x0100, (short)0x01FF);
        run(cpu, 100);
        dumpMemory(retro24, (short) 0x0100, (short) 0x01FF);
        
    }
    
    // Lässt die CPU endlos laufen bis sie angehalten wird.
    private static void run(CPU cpu, int hz) {
    	long instructionCount = 1;
    	long startTime = System.nanoTime();
    	int sleeptime = 1000/hz;
    	printRegisterState(cpu);   // Zeige Register nach der Ausführung
    	cpu.executeOpcode(); // Erste Instruktion ausführen
        // Starten
    	while (!cpu.istGestoppt()){
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
    
    // Lässt die CPU endlos laufen bis sie angehalten wird (einzelne Anweisung pro Tastendruck).
    private static void debug(CPU cpu) {
    	Scanner scan = new Scanner(System.in);
    	printRegisterState(cpu);   // Zeige Register nach der Ausführung
    	cpu.executeOpcode(); // Erste Instruktion ausführen
        // Starten
    	while (!cpu.istGestoppt()){
    		scan.nextLine();
        	printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
            printRegisterState(cpu);   // Zeige Register nach der Ausführung
        	cpu.executeOpcode();
    	}
        printLastOpcode(cpu);		// Zeige zuletzt ausgeführten Opcode
        printRegisterState(cpu);   // Zeige Register nach der Ausführung
        
        scan.close();
    }

    // Hilfsmethode, um den aktuellen Zustand der Register und des Programms auszugeben
    private static void printRegisterState(CPU cpu) {
        System.out.println("Registerinhalt:");
        System.out.println("R0: " + String.format("0x%02X", cpu.getR0()));
        System.out.println("R1: " + String.format("0x%02X", cpu.getR1()));
        System.out.println("R2: " + String.format("0x%02X", cpu.getR2()));
        System.out.println("R3: " + String.format("0x%02X", cpu.getR3()));
        System.out.println("IC: " + String.format("0x%04X", cpu.getIC()));
        System.out.println("AR: " + String.format("0x%04X", cpu.getAR()));
        System.out.println("########################");
    }
    
    // Hilfsmethode, um die Details des letzten Opcodes auszugeben
    private static void printLastOpcode(CPU cpu) {
    	System.out.println("Letzter Opcode:");
    	System.out.println("Opcode:	" + String.format("0x%02X", cpu.getLastOpcode()));
    	System.out.print  ("Args:	");
    	
    	String args = "";
    	
    	if (cpu.getLastOpcodeArgs() == null) {
    		System.out.println("keine");
    	}
    	else {
    		for (int i=0; i < cpu.getLastOpcodeArgs().length; i++) {
        		byte arg = cpu.getLastOpcodeArgs()[i];
        		System.out.print(String.format("0x%02X", arg));
        		args += (String.format("0x%02X", arg));
        		if (i < cpu.getLastOpcodeArgs().length - 1) {
        			System.out.print(", ");
        			args += " ";
        		}
    		}
    		System.out.println();
    	}
    	System.out.println("Len:	" + cpu.getlastOpcodeLen() + " Byte");
    	System.out.println(cpu.getLastAssembler() + " " + args);
    	System.out.println("------------------------");
    	
    }
    
    // Methode, um einen bestimmten Bereich des Speichers auszugeben
    private static void dumpMemory(Retro24 retro24, short from, short to) {
        // Umwandlung von short zu unsigned int durch Bitmaskierung
        int uFrom = from & 0xFFFF; // Maske für die unteren 16 Bits (von 0 bis 65535)
        int uTo = to & 0xFFFF;

        // Überprüfen, ob die Bereichsgrenzen sinnvoll sind
        if (uFrom > uTo || uFrom < 0x0000 || uTo > 0xFFFF) {
            System.out.println("MemoryDump: Ungültiger Bereich!");
            return;
        }

        System.out.println("Speicherauszug von " + String.format("0x%04X", from) + " bis " + String.format("0x%04X", to) + ":");

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