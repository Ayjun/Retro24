package core.CPU;

import static core.CPU.Instruction.HLT;
import static core.CPU.Instruction.getInstruction;
import static util.NumberUtil.*;
import static core.Retro24.TICKADDRESS;
import static core.Retro24.TOCKADDRESS;

import core.Retro24;

/**
 * Repräsentation einer fiktiven Retro24 CPU
 * @author Eric Schneider
 */
public class CPU {
	private static final int MAXOPERATIONLENGTH = 3;
	
	// CPU-Register:
	private byte R0;

	private byte R1;
	private byte R2;
	private byte R3;
	private short IC;
	private short AR;

	private Instruction lastInstruction = HLT;
	
	// Tick & Tock
	private byte tick;
	private byte tock;

	// Ist CPU angehalten?
	private boolean halt = false;
	
	// Information darüber ob die letzte IC Änderung ein Sprung war:
	private boolean jumped = false;

	// Referenz auf das Gesamtsystem
	private final Retro24 retro24;

	public CPU(Retro24 retro24) {
        this.retro24 = retro24;
    }

	/**
	 * Initialisiert die CPU
	 */
    public void initCPU() {
    	R0 = 0x00;
    	R1 = 0x00;
    	R2 = 0x00;
    	R3 = 0x00;
    	IC = 0x0100;
    	AR = 0x0000;
    	
    	tick = 0x00;
    	tock = (byte)0xFF;
    }

    /**
     * Ausführen der auf in IC verwiesenen Instruktion
     */
    public void executeOpcode() {
    	byte opcode = retro24.readMemory(IC);  // Lese das Byte an der Adresse IC
    	executeOpcode(opcode);
    }

    public void executeOpcode(byte opcode) {
    	Instruction newInstruction = getInstruction(opcode);
    	newInstruction.execute(this);
    	moveIC(newInstruction.getLength());
    	tickTock();
    	setLastInstruction(newInstruction);
    }

	public void writeMemory(int address, byte data) {
		this.retro24.writeMemory(address, data);
	}
	
	/**
	 * Ruft die Methode readMemory des retro24 Systems.
	 * @see Retro24.readMemory
	 * @param address
	 * @return Daten an der Adresse
	 */
	public byte readMemory(int address) {
		return retro24.readMemory(address);
	}
	
	/**
	 * Liest das byte Daten von der Speicheradresse die in AR steht.
	 * @return das byte
	 */
	public byte readFromAR() {
		return readMemory(unsign(AR));
	}
	
	/**
	 * Liest das byte Daten von der Speicheradresse die in AR + offset steht.
	 * @param offset das auf die Adresse addiert wird
	 * @return das byte
	 */
	public byte readFromAR(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset < 0 is not allowed!");
		}
		return readMemory(addU(AR, offset));
	}
	
	/**
	 * Schreibt ein byte array nach Adresse in AR, erstindexiertes Element an erster (kleinerer) Adresse
	 * @param data (das Array)
	 */
    public void writeToARIncrementing(byte[] data) {
    	for (int i = 0; i < data.length; i++) {
    		retro24.writeMemory(addU(AR, i), data[i]);
    	}
    }

    /**
     * Schreibt ein byte array nach Adresse in AR, letztindexiertes Element an erster (kleinerer) Adresse
     * @param data (das Array)
     */
    public void writeToARDecrementing(byte[] data) {
    	for (int i = data.length - 1, j = 0; i >= 0; i--, j++) {
    		retro24.writeMemory(addU(AR, j), data[i]);
    	}
    }

    /**
     * Liefert das X-te Argmunet des Opcodes (X = number), beginnt bei 0.
     * @param number
     * @return
     */
    public byte getOpcodeArgument(int position) {
    	// Argument 0 ist an Position ic + 1, daher erhöhe number um 1
    	position += 1;
    	
    	// Wenn die Postion > maximaler Operationslänge, Exception:
    	if (position >= MAXOPERATIONLENGTH || position < 0) {
    		throw new IllegalArgumentException("Invalid argument index: " + position + ". Must be between 0 and " + (MAXOPERATIONLENGTH - 1) + ".");
    	}
    	
    	// Addiere die Position unsigned auf IC
    	int argAddress = addU(IC, position);

    	// Wenn es keine Exception gab, in readMemory dort wird auch auf OutOfBounds geprüft:
    	return this.retro24.readMemory(trimToShort(argAddress));
    }

    /**
     * Verschiebt den InstructionCounter um die OpcodeLength. Führt bei überschreiten von 0xFFFF zu einem wrap around.
     * (Package private zu Testzwecken)
     */
    public void moveIC(int moveBy) {
    	
    	// Wenn CPU angehalten oder wenn IC gejumpt wurde IC nicht weiterschieben.
    	if (isHalted() || hasJumped()) {
    		setJumped(false);
    		return;
    	}
    	
    	int newIC = unsign(IC) + moveBy;

    	// Behalte nur die letzten 16 Bit, dies ermöglicht auch wrap around:
    	IC = trimToShort(newIC);
    }
    
    /**
     * Aktualisiert tick und tock und speichert die Werte an entsprecehenden Stellen
     */
    private void tickTock() {
    	tick = trimToByte(addU(tick, 1));
    	if (!checkUnderflow(subU(tock, 1))) {
    		tock = trimToByte(subU(tock, 1));
    	}
    	writeMemory(TICKADDRESS, tick);
    	writeMemory(TOCKADDRESS, tock);
    }
    
    public boolean hasJumped() {
    	return jumped;
    }

    public boolean isHalted() {
    	return halt;
    }

	public void setIC(short value) {
        IC = value;
    }
	
	public void jumpIC(short value) {
		setJumped(true);
		IC = value;
	}

    public short getIC() {
        return IC;
    }

    public void setAR(short value) {
        AR = value;
    }

    public short getAR() {
        return AR;
    }

	public byte getR0() {
		return R0;
	}

	public byte getR1() {
		return R1;
	}

	public byte getR2() {
		return R2;
	}

	public byte getR3() {
		return R3;
	}

	public void setR0(byte r0) {
		R0 = r0;
	}

	public void setR1(byte r1) {
		R1 = r1;
	}

	public void setR2(byte r2) {
		R2 = r2;
	}

	public void setR3(byte r3) {
		R3 = r3;
	}

	public void setHalt(boolean halt) {
		this.halt = halt;
	}
	
	public void setJumped(boolean jumped) {
		this.jumped = jumped;
	}

	public Instruction getLastInstruction() {
		return lastInstruction;
	}

	public void setLastInstruction(Instruction lastInstruction) {
		this.lastInstruction = lastInstruction;
	}
}
