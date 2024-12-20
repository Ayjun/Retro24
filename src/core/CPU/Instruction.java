package core.CPU;

import static common.util.NumberUtil.*;

import java.util.HashMap;

import core.exceptions.InvalidOpcodeException;

/**
 * Enum aller CPU Instruktionen, die Instruktionen enthalten jeweils
 * ihre Assembler Repräsentation als String, den Opcode als Bytewert und ihre
 * tatsächliche Funktion in Form eines OpcodeOperation Objektes (funktionales
 * Interface).
 * Sämtliche Instruktionen werden in einer HashMap abgelegt, auf die über die
 * getInstruction() Funktion zugegriffen wird. Somit gibt es ein einfaches Mapping
 * von Opcode (byte) auf die Instruktion (Instruction), die alle Details enthält.
 * @see core.CPU.OpcodeOperation
 * @author Eric Schneider
 */
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
		// nutze daher Decrementing um das lowByte zuerst zu schreiben
		cpu.writeToARDecrementing(bytes);

		return new byte[0];
	}),

	// RAR ($03, 1-Byte-OP): R1/R2 werden ins AR kopiert.
	RAR("RAR", 0x03, (cpu) -> {
		
		// TESTWEISE mal drehen OBEN IST ORIGINAL:
		short newAR = twoByteToShort(cpu.getR2(), cpu.getR1());
		// FALSCHHERUM, ABER HABEN MANCHE RETRO EVTL SO: 
		// short newAR = twoByteToShort(cpu.getR1(), cpu.getR2());
		cpu.setAR(newAR);

		return new byte[0];
	}),
	
	// AAR ($04, 1-Byte-OP): Addiert R0 aufs AR, bei Überlauf geht Übertrag verloren.
	AAR("AAR", 0x04, (cpu) -> {
		cpu.setAR(trimToShort(addU(cpu.getR0(), cpu.getAR())));
		
		return new byte[0];
	}),
	
	// IR0 ($05, 1-Byte-OP): Erhöht den Wert von R0 um 1, allerdings nicht über $FF hinaus
	IR0("IR0", 0x05, (cpu) -> {
		if (checkByteOverflow(addU(cpu.getR0(), (byte)1))) {
			return new byte[0];
		}
		
		cpu.setR0(trimToByte(addU(cpu.getR0(), (byte)1)));
		
		return new byte[0];
	}),
	
	// A01 ($06, 1-Byte-OP): Addiert R0 auf R1. Bei Überlauf wird R2 um 1 erhöht.
	// Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
	A01("A01", 0x06, (cpu) -> {
		boolean R1Overflown = false;
		boolean R2Overflown = false;
		
		// Addiert R0 auf R1
		int sumR0R1 = addU(cpu.getR0(), cpu.getR1()); 
		
		// R1 neu setzen:
		cpu.setR1(trimToByte(sumR0R1));
		
		// R1 Overflow prüfen und merken
		R1Overflown = checkByteOverflow(sumR0R1);

		// Bei Überlauf R1:
		if (R1Overflown) {
			// R2 neu berechnen
			int R2Incremented = addU(cpu.getR2(), (byte) 1);
			
			// R2 neu setzen:
			cpu.setR2(trimToByte(R2Incremented));
			
			// R2 auf Überlauf prüfen und merken:
			R2Overflown = checkByteOverflow(R2Incremented);
		}
		
		// Wenn R2 übergelaufen ist:
		if (R2Overflown) {
			cpu.setR1((byte) 0xFF);
			cpu.setR2((byte) 0xFF);
		}
		
		return new byte[0];
	}),
	
	//DR0 ($07, 1-Byte-OP): Erniedrigt den Wert von R0 um 1, allerdings nicht unter $00.
	DR0("DR0", 0x07, (cpu) -> {

		int result = subU(cpu.getR0(), (byte) 1);
		cpu.setR0((byte) Math.max(0, result));

		return new byte[0];
	}),
	
	// S01 ($08, 1-Byte-OP): Subtrahiert R0 von R1. Falls eine negative Zahl
	// entsteht, enthält R1 dann den Betrag der negativen Zahl. Ferner wird dann
	// R2 um 1 erniedrigt. Tritt dabei ein Unterlauf von R2 auf, werden R1 und R2
	// zu $00.
	S01("S01", 0x08, (cpu) -> {
		boolean R1Underflown = false;
		boolean R2Underflown = false;
		
		// Subtrahiert R0 von R1
		int diffR1R0 = subU(cpu.getR1(), cpu.getR0());
		// R1 neu setzen:
		cpu.setR1(trimToByte(diffR1R0));
		
		// R1 Underflow prüfen und merken
		R1Underflown = checkUnderflow(diffR1R0);
		
		if (R1Underflown) {
			// Auf absoluten Wert setzen:
			cpu.setR1(trimToByte(Math.abs(diffR1R0)));
			
			// R2 dektremntieren
			int R2Decremented = subU(cpu.getR2(), (byte) 1);
			cpu.setR2(trimToByte(R2Decremented));
			
			// R2 Underflow prüfen und merken
			R2Underflown = checkUnderflow(R2Decremented);
		}
		
		if (R2Underflown) {
			cpu.setR1((byte) 0x00);
			cpu.setR2((byte) 0x00);
		}
		
		return new byte[0];
	}),
	
	// X12 ($09, 1-Byte-OP): Vertauscht die Inhalte von R1 und R2.
	X12("X12", 0x09, (cpu) -> {
		byte R1Old = cpu.getR1();
		byte R2Old = cpu.getR2();

		cpu.setR1(R2Old);
		cpu.setR2(R1Old);
		
		return new byte[0];
	}),
	
	// X01 ($10, 1-Byte-OP): Vertauscht die Inhalte von R0 und R1.
	X01("X01", 0x10, (cpu) -> {
		byte R0Old = cpu.getR0();
		byte R1Old = cpu.getR1();

		cpu.setR0(R1Old);
		cpu.setR1(R0Old);
		
		return new byte[0];
	}),
	
	// JMP ($11, 1-Byte-OP): Springt zu der in AR angegebenen Adresse.
	JMP("JMP", 0x11, (cpu) -> {
		cpu.jumpIC(cpu.getAR());
		
		return new byte[0];
	}),
	
	// SR0 ($12, 1-Byte-OP): Speichert R0 an die in AR angegebene Adresse.
	SR0("SR0", 0x12, (cpu) -> {
		cpu.writeMemory(unsign(cpu.getAR()), cpu.getR0());
		return new byte[0];
	}),
	
	// SRW ($13, 1-Byte-OP): 
	// Speichert R1 an die in AR angegebene Adresse,ferner R2 an die Adresse dahinter.
	SRW("SRW", 0x13, (cpu) -> {
		cpu.writeToARIncrementing(new byte[] {cpu.getR1(), cpu.getR2()});
		
		return new byte[0];
	}),
	
	// LR0 ($14, 1-Byte-OP): Lädt R0 aus der in AR angegebenen Adresse.
	LR0("LR0", 0x14, (cpu) -> {
		cpu.setR0(cpu.readFromAR());
		
		return new byte[0];
	}),
	
	// LRW ($15, 1-Byte-OP): Lädt R1 aus der in AR angegebenen Adresse,
	// ferner R2 aus der Adresse dahinter.
	LRW("LRW", 0x15, (cpu) -> {
		cpu.setR1(cpu.readFromAR());
		cpu.setR2(cpu.readFromAR(1));
		return new byte[0];
	}),
	
	// TAW ($16, 1-Byte-OP): AR wird nach R1/R2 kopiert.
	TAW("TAW", 0x16, (cpu) -> {
		byte[] ARbytes = shortToByteArray(cpu.getAR());
		byte highByte = ARbytes[0];
		byte lowByte = ARbytes[1];
		
		// TESTWEISE mal drehen OBEN IST ORIGINAL:
		
		cpu.setR1(lowByte);
		cpu.setR2(highByte);
		
		// FALSCHHERUM! ABER IST IN MANCHEN RETRO SO:
		//cpu.setR1(highByte);
		//cpu.setR2(lowByte);
		return new byte[0];
	}),

	// MR0 ($17, 2-Byte-OP): Das nachfolgende Byte wird nach R0 geschrieben.
	MR0("MR0", 0x17, (cpu) -> {
		byte arg = cpu.getOpcodeArgument(0);
		cpu.setR0(arg);
		return new byte[] {arg};
	}),
	
	// MRW ($18, 3-Byte-OP): Die nachfolgenden 2 Bytes werden nach R1 und R2 geschrieben.
	MRW("MR0", 0x18, (cpu) -> {
		byte R1 = cpu.getOpcodeArgument(0);
		byte R2 = cpu.getOpcodeArgument(1);
		
		cpu.setR1(R1);
		cpu.setR2(R2);
		
		return new byte[] {R1, R2};
	}),
	
	// JZ0 ($19, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls R0=$00 ist.
	JZ0("JZ0", 0x19, (cpu) -> {
		if (cpu.getR0() == 0x00) {
			cpu.jumpIC(cpu.getAR());
		}
		
		return new byte[0];
	}),
	
	// JGW ($20, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls
    //	R1 > R2 ist.
	JGW("JGW", 0x20, (cpu) -> {
		if (unsign(cpu.getR1()) > unsign(cpu.getR2())) {
			cpu.jumpIC(cpu.getAR());
		}
		
		return new byte[0];
	}),
	
	// JEW ($21, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls
	// R1=R2 ist
	JEW("JEW", 0x21, (cpu) -> {
		if (unsign(cpu.getR1()) == unsign(cpu.getR2())) {
			cpu.jumpIC(cpu.getAR());
		}
		
		return new byte[0];
	}),
	
	// OR0 ($22, 2-Byte-OP): Speichert in R0 das logische ODER aus dem
    //	aktuellen Wert von R0 und dem nachfolgenden Byte
	OR0("OR0", 0x22, (cpu) -> {
		byte arg = cpu.getOpcodeArgument(0);
		cpu.setR0(trimToByte(uOr(arg, cpu.getR0())));
		
		return new byte[] {arg};
	}),
	
	// AN0 ($23, 2-Byte-OP): Speichert in R0 das logische UND aus dem
	//	aktuellen Wert von R0 und dem nachfolgenden Byte.
	AN0("AN0", 0x23, (cpu) -> {
		byte arg = cpu.getOpcodeArgument(0);
		cpu.setR0(trimToByte(uAnd(arg, cpu.getR0())));
		
		return new byte[] {arg};
	}),

	// JE0 ($24, 2-Byte-OP): Springt zu der in AR angegebenen Adresse, falls R0
	//	gleich dem nachfolgenden Byte ist
	JE0("JE0", 0x24, (cpu) -> {
		byte arg = cpu.getOpcodeArgument(0);
		
		if(unsign(arg) == unsign(cpu.getR0())) {
			cpu.jumpIC(cpu.getAR());
		}
		
		return new byte[] {arg};
	}),
	
	// C01 ($25, 1-Byte-OP): Kopiert R0 nach R1.
	C01("C01", 0x25, (cpu) -> {
		cpu.setR1(cpu.getR0());

		return new byte[0];
	}),
	
	// C02 ($26, 1-Byte-OP): Kopiert R0 nach R2.
	C02("C02", 0x26, (cpu) -> {
		cpu.setR2(cpu.getR0());
		
		return new byte[0];
	}),
	
	// IRW ($27, 1-Byte-OP): Erhöht den Wert von R1 um 1. Bei Überlauf wird R2
	//	um 1 erhöht. Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
	IRW("IRW", 0x27, (cpu) -> {
		boolean R1Overflown = false;
		boolean R2Overflown = false;
		
		// Addiert 1 auf R1
		int incrementedR0 = addU(cpu.getR1(), 1); 
		
		// R1 neu setzen:
		cpu.setR1(trimToByte(incrementedR0));
		
		// R1 Overflow prüfen und merken
		R1Overflown = checkByteOverflow(incrementedR0);

		// Bei Überlauf R1:
		if (R1Overflown) {
			// R2 neu berechnen
			int R2Incremented = addU(cpu.getR2(), (byte) 1);
			
			// R2 neu setzen:
			cpu.setR2(trimToByte(R2Incremented));
			
			// R2 auf Überlauf prüfen und merken:
			R2Overflown = checkByteOverflow(R2Incremented);
		}
		
		// Wenn R2 übergelaufen ist:
		if (R2Overflown) {
			cpu.setR1((byte) 0xFF);
			cpu.setR2((byte) 0xFF);
		}
		
		return new byte[0];
	}),
	
	// DRW ($28, 1-Byte-OP): Erniedrigt den Wert von R1 um 1. Falls eine
	// negative Zahl entsteht, enthält R1 dann den Betrag der negativen Zahl.
	// Ferner wird dann R2 um 1 erniedrigt. Tritt dabei ein Unterlauf von R2 auf,
	// werden R1 und R2 zu $00.
	DRW("DRW", 0x28, (cpu) -> {
		boolean R1Underflown = false;
		boolean R2Underflown = false;
		
		// Subtrahiert R0 von R1
		int decrementedR1 = subU(cpu.getR1(), 1);
		// R1 neu setzen:
		cpu.setR1(trimToByte(decrementedR1));
		
		// R1 Underflow prüfen und merken
		R1Underflown = checkUnderflow(decrementedR1);
		
		if (R1Underflown) {
			// Auf absoluten Wert setzen:
			cpu.setR1(trimToByte(Math.abs(decrementedR1)));
			
			// R2 dektremntieren
			int R2Decremented = subU(cpu.getR2(), (byte) 1);
			cpu.setR2(trimToByte(R2Decremented));
			
			// R2 Underflow prüfen und merken
			R2Underflown = checkUnderflow(R2Decremented);
		}
		
		if (R2Underflown) {
			cpu.setR1((byte) 0x00);
			cpu.setR2((byte) 0x00);
		}
		
		return new byte[0];
	}),
	
	// X03 ($29, 1-Byte-OP): Vertauscht die Inhalte von R0 und R3.
	X03("X03", 0x29, (cpu) -> {
		byte oldR0 = cpu.getR0();
		byte oldR3 = cpu.getR3();

		cpu.setR0(oldR3);
		cpu.setR3(oldR0);
		
		return new byte[0];
	}),
	
	// C03 ($2A, 1-Byte-OP): Kopiert R0 nach R3.
	C03("C03", 0x2A, (cpu) -> {
		cpu.setR3(cpu.getR0());
		
		return new byte[0];
	}),
	
	// C30 ($2B, 1-Byte-OP): Kopiert R3 nach R0.
	C30("C30", 0x2B, (cpu) -> {
		cpu.setR0(cpu.getR3());
		
		return new byte[0];
	}),
	
	// PL0 ($2C, 1-Byte-OP): Schiebt die Bits in R0 um ein Bit nach
	//	„links“ (entspricht Teilen ?MULTIPLIZIEREN? durch 2 ohne Rest)
	PL0("PL0", 0x2C, (cpu) -> {
		cpu.setR0(trimToByte((unsign(cpu.getR0()) << 1)));
		return new byte[0];
	}),
	
	// PR0 ($2D, 1-Byte-OP): Schiebt die Bits in R0 um ein Bit nach
	// „rechts“ (entspricht Multiplikation ?DIVISION? mit 2 ohne Übertrag).
	PR0("PR0", 0x2D, (cpu) -> {
		cpu.setR0(trimToByte((unsign(cpu.getR0()) >>> 1)));
		return new byte[0];
	}),

	// HLT ($FF, 1-Byte-OP): Prozessor hält an.
	HLT("HLT", 0xFF, (cpu) -> {
		cpu.setHalt(true);

		return new byte[0];
	});


	// STATIC PART:

	private final static HashMap<Byte, Instruction> instructions;
	
	// Initialisieren der HashMap:
	static {
		instructions = new HashMap<>();

		for (Instruction instruction : Instruction.values()) {
			instructions.put(instruction.getOpcode(), instruction);
		}
	}

	public static Instruction getInstruction(byte opcode) throws InvalidOpcodeException {

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

	/**
	 * Führt eine Instruktion aus und speichert die Argumente dieser im args Attribut. 
	 * @param cpu die CPU auf der die Instruktion ausgeführt wird
	 */
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
