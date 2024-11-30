package core.CPU;

import static util.NumberUtil.*;

import java.util.HashMap;

import exceptions.InvalidOpcodeException;

public enum Instruction {
	
	NUL("NUL", 0x00, (cpu) -> {
		return new byte[0];
	}),
	
	// MAR ($01, 3-Byte-OP): Lädt AR mit den nächsten beiden Bytes.
	MAR("MAR", 0x01, (cpu) -> {
		// Arugmente holen
		byte lowByte = cpu.getOpcodeArgument(0);
		byte highByte = cpu.getOpcodeArgument(1);
		
		// Kombinieren der beiden Bytes zu einem 16-Bit Wert
	    short address = twoByteToShort(highByte, lowByte);
	    
	    // Adressregister setzen
	    cpu.setAR(address);
	    
	    // Argumente speichern
	    return new byte[] {lowByte, highByte};
	}),
	
 	// SIC ($02, 1-Byte-OP): Speichert IC an die im AR angegebene Adresse.
	SIC("SIC", 0x02, (cpu) -> {
		// IC auslesen:
		byte[] bytes = shortToByteArray(cpu.getIC());
	
		// highbyte ist an bytes[0] lowbyte an bytes[1]
		// nutze daher decrementing um das lowbyte zuerst zu schreiben
		cpu.writeToARDecrementing(bytes);
		
		return new byte[0];
	}),
	
	// Ich gehe davon aus es ist gemeint R1 und R2 sollen der Inhalt fuer AR werden?
	// Weiterhin gehe ich davon aus dass R1 das höherwertige Byte sein soll?
	// RAR ($03, 1-Byte-OP): R1/R2 werden ins AR kopiert.
	RAR("RAR", 0x03, (cpu) -> {

		short newAR = twoByteToShort(cpu.getR1(), cpu.getR2());

		cpu.setAR(newAR);
		
		return new byte[0];
	}),
	
	//DR0 ($07, 1-Byte-OP): Erniedrigt den Wert von R0 um 1, allerdings nicht unter $00.
	DR0("DR0", 0x07, (cpu) -> {
		
		int result = subU(cpu.getR0(), (byte) 1);
		cpu.setR0((byte) Math.max(0, result));
		
		return new byte[0];
	}),
	
	// MR0 ($17, 2-Byte-OP): Das nachfolgende Byte wird nach R0 geschrieben.
	MR0("MR0", 0x17, (cpu) -> {
		byte arg = cpu.getOpcodeArgument(0);
		cpu.setR0(arg);
		return new byte[] {arg};
	}),

	// C01 ($25, 1-Byte-OP): Kopiert R0 nach R1.
	C01("C01", 0x25, (cpu) -> {
		cpu.setR1(cpu.getR0());
		
		return new byte[0];
	}),
	
	HLT("HLT", 0xFF, (cpu) -> {
		cpu.setHalt(true);
		
		return new byte[0];
	});
	
	
	// STATIC PART:
	
	private final static HashMap<Integer, Instruction> instructions;
	
	static {
		instructions = new HashMap<Integer, Instruction>();
		
		instructions.put(0x00, NUL);
		instructions.put(0x01, MAR);
		instructions.put(0x02, SIC);
		instructions.put(0x03, RAR);
		
		instructions.put(0x07, DR0);
		
		instructions.put(0x17, MR0);
		
		instructions.put(0x25, C01);
	}
	
	public static Instruction getInstruction(int opcode) throws InvalidOpcodeException {
		
		if (instructions.get(opcode) == null) {
			throw new InvalidOpcodeException("Invalid Opcode: " + String.format("0x%02X", opcode));
		}
		return instructions.get(opcode);
	}
	
	private final String assemblerCode;
	private final byte opcode;
	private final OpcodeOperation function;
	private byte[] args;
	
	
	// NON STATIC:
	
	Instruction(String assemblerCode, int opcode, OpcodeOperation function) {
		this.assemblerCode = assemblerCode;
		this.opcode = (byte) (opcode & 0xFF);
		this.function = function;
	}
	
	public void execute(CPU cpu) {
		args = function.execute(cpu);
	}
	
	public byte[] getArgs() {
		return args;
	}

	public void setArgs(byte[] args) {
		this.args = args;
	}

	public String getAssemblerCode() {
		return assemblerCode;
	}

	public byte getOpcode() {
		return opcode;
	}
	
	/**
	 * 
	 * @return die Länge der Instruktion / Operation
	 */
	public int getLength() {
		return this.args.length + 1;
	}
}
