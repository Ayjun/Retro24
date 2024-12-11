package util;

import core.Retro24;
import core.CPU.CPU;

public class DebugUtil {
	public static String dumpMemory(Retro24 retro24, int memStart, int memEnd) {
	    if (memStart > memEnd || memStart < Retro24.MEMORYSTART || memEnd > Retro24.MEMORYEND) {
	        throw new IllegalArgumentException("Ungültiger Speicherbereich! Von: " + memStart + " bis: " + memEnd);
	    }

	    StringBuilder dump = new StringBuilder();
	    dump.append("Speicherauszug von ")
	        .append(String.format("0x%04X", memStart))
	        .append(" bis ")
	        .append(String.format("0x%04X", memEnd))
	        .append(":")
	        .append(System.lineSeparator());

	    for (int address = memStart; address <= memEnd; address += 16) {
	        dump.append(String.format("0x%04X: ", address));

	        for (int offset = 0; offset < 16 && (address + offset) <= memEnd; offset++) {
	            byte value = retro24.readMemory((short) (address + offset));
	            dump.append(String.format("%02X ", value));
	        }
	        dump.append(System.lineSeparator());
	    }
	    return dump.toString();
	}
	
	public static String dumpLastCPUInstruction(Retro24 retro24) {
		CPU cpu = retro24.getCPU();
		
		StringBuilder dump = new StringBuilder();
		
		dump.append("Registerinhalt:");
		dump.append(System.lineSeparator());
		dump.append("R0: " + String.format("0x%02X", cpu.getR0()));
		dump.append(System.lineSeparator());
		dump.append("R1: " + String.format("0x%02X", cpu.getR1()));
		dump.append(System.lineSeparator());
		dump.append("R2: " + String.format("0x%02X", cpu.getR2()));
		dump.append(System.lineSeparator());
		dump.append("R3: " + String.format("0x%02X", cpu.getR3()));
		dump.append(System.lineSeparator());
		dump.append("IC: " + String.format("0x%04X", cpu.getIC()));
		dump.append(System.lineSeparator());
		dump.append("AR: " + String.format("0x%04X", cpu.getAR()));
		dump.append(System.lineSeparator());
		dump.append("########################");
		dump.append(System.lineSeparator());
		
		return dump.toString();
	}
}
