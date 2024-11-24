package core.CPU;

import core.Retro24;

public class CPU {
	// CPU-Register:
	private byte R0;

	private byte R1;
	private byte R2;
	private byte R3;
	private short IC;
	private short AR;
	
	// Hilfsvariablen
	private byte lastOpcode = (byte) 0xFF; // Letzter Opcode ist beim Start FF, da CPU still stand :)
	private byte[] lastOpcodeArgs = null;
	private byte lastOpcodeLen = 0;
	private String lastAssembler = "";
	
	private boolean halt = false;
	
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
    }
    
    /**
     * Ausführen der auf in IC verwiesenen Instruktion
     */
    public void executeOpcode() {
    	byte opcode = retro24.readMemory(IC);  // Lese das Byte an der Adresse IC
    	executeOpcode(opcode);
    }
    	
    
    /**
     * Führt die zum übergebenen Opcode gehörige Funktion aus
     * @param opcode
     */
    public void executeOpcode(byte opcode) {

        // Hier wird der Opcode über einen Switch-Case behandelt
        switch (opcode) {
        	
        	// NUL ($00, 1-Byte-OP): Prozessor tut nichts
        	case 0x00:
        		
        		lastOpcode = 0x00;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "NUL";
        		
        		IC += 1;
        		break;
        	
        	// MAR ($01, 3-Byte-OP): Lädt AR mit den nächsten beiden Bytes.
        	case 0x01:
        		byte lowByte = retro24.readMemory((short) (IC+1));
        		byte highByte = retro24.readMemory((short) (IC+2));
        		// Kombinieren der beiden Bytes zu einem 16-Bit Wert
        	    short address = (short) (((highByte & 0xFF) << 8) | (lowByte & 0xFF));
        	    AR = address;
        	    
        	    lastOpcode = 0x01;
        		lastOpcodeArgs = new byte[] {lowByte, highByte};
        		lastOpcodeLen = 3;
        		lastAssembler = "MAR";
        		
        	    IC += 3;
        	    break;
        	
        	// SIC ($02, 1-Byte-OP): Speichert IC an die im AR angegebene Adresse.
        	case 0x02:
        		int lowByteI = IC & 0xFF;
        		int highByteI = IC >> 8 & 0xFF;
        	    lowByte = (byte)lowByteI;
        	    highByte = (byte)highByteI;
        		
        		retro24.writeMemory(AR, lowByte);
        		retro24.writeMemory(AR+1, highByte);
        		
        		lastOpcode = 0x02;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "SIC";
        		
        		IC += 1;
        		break;
        	
        	// Ich gehe davon aus es ist gemeint R1 gefolgt von R2 sollen der Inhalt fuer AR werden?
        	// Weiterhin gehe ich davon aus dass R1 das höherwertige Byte sein soll?
        	// RAR ($03, 1-Byte-OP): R1/R2 werden ins AR kopiert.
        	case 0x03:
        		 highByte = R1;
        		 lowByte = R2;
        		
        		short newAR = (short) ((R1 << 8) | R2);
        		
        		AR = newAR;
        		
        		lastOpcode = 0x03;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "RAR";
        		
        		IC += 1;
        		break;
        		
        	// AAR ($04, 1-Byte-OP): Addiert R0 aufs AR, bei Überlauf geht Übertrag verloren.
        	case 0x04:
        		int iR0 = R0 & 0xFF;
        		int iAR = AR & 0xFFFF;
        		AR = (short) ((iAR + iR0) & 0xFFFF);
        		
        		lastOpcode = 0x04;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "AAR";
        		
        		IC += 1;
        		break;
        	
        	// IR0 ($05, 1-Byte-OP): Erhöht den Wert von R0 um 1, allerdings nicht über $FF hinaus
        	case 0x05:
        		int uR0 = R0 & 0xFF;
        		if (!(uR0 + 1 > 0xFF)) {
        			R0 += 1;
        		}
        		
        		lastOpcode = 0x05;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "IR0";
        		
        		IC += 1;
        		break;
   
        		
			// A01 ($06, 1-Byte-OP): Addiert R0 auf R1. Bei Überlauf wird R2 um 1 erhöht.
			// Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
			case 0x06:
			    int uR1 = R1 & 0xFF; // Wandelt R1 in einen unsigned 8-Bit-Wert
			    int uR2 = R2 & 0xFF; // Wandelt R2 in einen unsigned 8-Bit-Wert
			    uR0 = R0 & 0xFF; // Wandelt R0 in einen unsigned 8-Bit-Wert
	
			    // Berechne R1 + R0 und prüfe auf Überlauf
			    if (uR1 + uR0 > 0xFF) { // Überlauf bei R1
			        // R2 erhöhen und prüfen, ob R2 überläuft
			        if (uR2 + 1 > 0xFF) {
			            // Überlauf bei R2: Beide Register auf 0xFF setzen
			            R1 = (byte) 0xFF;
			            R2 = (byte) 0xFF;
			        } else {
			            // Kein Überlauf bei R2: R2 erhöhen und R1 korrekt setzen
			            R2 = (byte) ((uR2 + 1) & 0xFF);
			            R1 = (byte) ((uR1 + uR0) & 0xFF);
			        }
			    } else {
			        // Kein Überlauf bei R1: Nur R1 setzen
			        R1 = (byte) ((uR1 + uR0) & 0xFF);
			    }
	
			    lastOpcode = 0x06;
			    lastOpcodeArgs = null;
			    lastOpcodeLen = 1;
			    lastAssembler = "A01";
	
			    IC += 1;
			    break;

        	//DR0 ($07, 1-Byte-OP): Erniedrigt den Wert von R0 um 1, allerdings nicht unter $00.
        	case 0x07:
        		if (!(R0 == 0x00)) {
        			R0 -= 1;
        		}
        		
        		lastOpcode = 0x07;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "DR0";
        		
        		IC += 1;
        		break;
        	
    		// S01 ($08, 1-Byte-OP): Subtrahiert R0 von R1. Falls eine negative Zahl
    		// entsteht, enthält R1 dann den Betrag der negativen Zahl. Ferner wird dann
    		// R2 um 1 erniedrigt. Tritt dabei ein Unterlauf von R2 auf, werden R1 und R2
    		// zu $00.
        	case 0x08:
        		uR0 = R0 & 0xFF;
        		uR1 = R1 & 0xFF;
        		
        		int difference = uR1 - uR0;
        		
        		// Falls Ergebnis negativ:
        		if (difference < 0) {
        			R1 = (byte) Math.abs(difference);
        			uR2 = R2 & 0xFF;
        			uR2 -= 1;
        			if (uR2 == 0xFF) { // Falls übergelaufen steht uR2 auf 0xFF
        				R1 = 0x00;
        				R2 = 0x00;
        			}
        			else {
        				R2 = (byte) (uR2 & 0xFF);
        			}
        		}
        		
        		lastOpcode = 0x08;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "S01";
        		
        		IC += 1;
        		break;
        	
        	// X12 ($09, 1-Byte-OP): Vertauscht die Inhalte von R1 und R2.
        	case 0x09:
        		byte R1Old = R1;
        		byte R2Old = R2;
        		
        		R1 = R2Old;
        		R2 = R1Old;
        		
        		lastOpcode = 0x09;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "X12";
        		
        		IC += 1;
        		break;
        	
        	// X01 ($10, 1-Byte-OP): Vertauscht die Inhalte von R0 und R1.
        	case 0x10:
        		byte R0Old = R0;
        		R1Old = R1;
        		
        		R0 = R1Old;
        		R1 = R0Old;
        		
        		lastOpcode = 0x10;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "X01";
        		
        		IC += 1;
        		break;
        	
        	// JMP ($11, 1-Byte-OP): Springt zu der in AR angegebenen Adresse.
        	case 0x11:
        		lastOpcode = 0x11;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "JMP";

        	
        		IC = AR;
        		
        		break;
        	
        	// SR0 ($12, 1-Byte-OP): Speichert R0 an die in AR angegebene Adresse.
        	case 0x12:
        		retro24.writeMemory(AR, R0);
        		
        		lastOpcode = 0x12;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "SR0";
        		
        		IC += 1;
        		
        		break;
        		
        	// SRW ($13, 1-Byte-OP): Speichert R1 an die in AR angegebene Adresse,ferner R2 an die Adresse dahinter.
        	case 0x13:
        		retro24.writeMemory(AR, R1);
        		retro24.writeMemory(AR + 1, R2);
        		
        		lastOpcode = 0x13;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "SRW";

        		IC += 1;
        		
        		break;
        		
        	// LR0 ($14, 1-Byte-OP): Lädt R0 aus der in AR angegebenen Adresse.
        	case 0x14:
        		R0 = retro24.readMemory(AR);
        		
        		lastOpcode = 0x14;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "LR0";

        		IC += 1;
        		
        		break;
        		
    		// LRW ($15, 1-Byte-OP): Lädt R1 aus der in AR angegebenen Adresse,
    		// ferner R2 aus der Adresse dahinter.
        	case 0x15:
        		R1 = retro24.readMemory(AR);
        		R2 = retro24.readMemory((short) (AR+1));
        		
        		lastOpcode = 0x15;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "LRW";

        		IC += 1;
        		
        		break;
        		
        	// TAW ($16, 1-Byte-OP): AR wird nach R1/R2 kopiert.
        	case 0x16:
        		R1 = (byte)((AR >> 8) & 0xFF);
        		R2 = (byte) (AR & 0xFF);
        		lastOpcode = 0x16;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "TAW";

        		IC += 1;
        		
        		break;
        	
        	// MR0 ($17, 2-Byte-OP): Das nachfolgende Byte wird nach R0 geschrieben.
            case 0x17:
            	R0 = retro24.readMemory((short) (IC + 1));
            	
            	lastOpcode = 0x17;
            	lastOpcodeLen = 2;
            	lastOpcodeArgs = new byte[] {retro24.readMemory((short) (IC + 1))};
        		lastAssembler = "MR0";

            	
            	IC += 2;            	
            	break;
            
            	
            // MRW ($18, 3-Byte-OP): Die nachfolgenden 2 Bytes werden nach R1 und R2 geschrieben.
            case 0x18:
                // R1 erhält das erste nachfolgende Byte
                R1 = retro24.readMemory((short) (IC+1));

                // R2 erhält das zweite nachfolgende Byte
                R2 = retro24.readMemory((short) (IC+2));

                // Opcode-Metadaten aktualisieren
                lastOpcode = 0x18;          // Letzter ausgeführter Opcode
                lastOpcodeArgs = new byte[]{retro24.readMemory((short) (IC + 1)), retro24.readMemory((short) (IC + 2))}; // Argumente speichern
                lastOpcodeLen = 3;          // Befehlslänge: 3 Bytes
                lastAssembler = "MRW";      // Assembler-Mnemonik

                // Befehlzähler erhöhen (um 3, da 3 Bytes verarbeitet wurden)
                IC += 3; // Program Counter um 3 erhöhen
                break;
                
            
            // JZ0 ($19, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls R0=$00 ist.
            case 0x19:
            	if (R0 == 0x00) {
            		IC = AR;
            	}
            	else {
            		IC += 1;
            	}
            	
            	lastOpcode = 0x19;
            	lastOpcodeLen = 1;
            	lastOpcodeArgs = null;
        		lastAssembler = "JZ0";

            	break;
            
            	
            // JGW ($20, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls
            //	R1 > R2 ist.
            case 0x20:
            	uR1 = R1 & 0xFF;
            	uR2 = R2 & 0xFF;
            	if (uR1 > uR2) {
            		IC = AR;
            	}
            	
            	lastOpcode = 0x20;
            	lastOpcodeLen = 1;
            	lastOpcodeArgs = null;
        		lastAssembler = "JGW";

            	break;
            	
        	// JEW ($21, 1-Byte-OP): Springt zu der in AR angegebenen Adresse, falls
        	// R1=R2 ist.
            case 0x21:
            	uR1 = R1 & 0xFF;
            	uR2 = R2 & 0xFF;
            	if (uR1 == uR2) {
            		IC = AR;
            	}
            	
            	lastOpcode = 0x21;
            	lastOpcodeLen = 1;
            	lastOpcodeArgs = null;
        		lastAssembler = "JEW";

            	break;
        
        	// OR0 ($22, 2-Byte-OP): Speichert in R0 das logische ODER aus dem
            //	aktuellen Wert von R0 und dem nachfolgenden Byte
            case 0x22:
            	byte argument = retro24.readMemory((short) (IC + 1));
            	
            	R0 = (byte) (R0 | argument);
        		
        		lastOpcode = 0x22;
        		lastOpcodeArgs = new byte[] {argument};
        		lastOpcodeLen = 2;
        		lastAssembler = "OR0";
        		
        		IC += 2;
        		break;
        		
        	// AN0 ($23, 2-Byte-OP): Speichert in R0 das logische UND aus dem
        	//	aktuellen Wert von R0 und dem nachfolgenden Byte.
            case 0x23:
            	argument = retro24.readMemory((short) (IC + 1));
            	
            	R0 = (byte) (R0 & argument);
        		
        		lastOpcode = 0x23;
        		lastOpcodeArgs = new byte[] {argument};
        		lastOpcodeLen = 2;
        		lastAssembler = "AN0";
        		
        		IC += 2;
        		break;
        		
        	// JE0 ($24, 2-Byte-OP): Springt zu der in AR angegebenen Adresse, falls R0
        	//	gleich dem nachfolgenden Byte ist
            case 0x24:
            	argument = retro24.readMemory((short) (IC + 1));
            	
            	if (R0 == argument) {
            		IC = AR;
            	}
            	else {
            		IC += 2;
            	}
        		
        		lastOpcode = 0x24;
        		lastOpcodeArgs = new byte[] {argument};
        		lastOpcodeLen = 2;
        		lastAssembler = "JE0";
        		
        		
        		break;
            
            // C01 ($25, 1-Byte-OP): Kopiert R0 nach R1.
        	case 0x25:
        		R1 = R0;
        		
        		lastOpcode = 0x25;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "C01";
        		
        		IC += 1;
        		break;
        		
        	// C02 ($26, 1-Byte-OP): Kopiert R0 nach R2.
        	case 0x26:
        		R2 = R0;
        		
        		lastOpcode = 0x26;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "C02";
        		
        		IC += 1;
        		break;
        		
    		// IRW ($27, 1-Byte-OP): Erhöht den Wert von R1 um 1. Bei Überlauf wird R2
        	//	um 1 erhöht. Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
        	case 0x27:
        		
        		uR1 = R1 & 0xFF; // Wandelt R1 in einen unsigned 8-Bit-Wert
			    uR2 = R2 & 0xFF; // Wandelt R2 in einen unsigned 8-Bit-Wert
	
			    // Erhöhe R1 um 1 und prüfe auf Überlauf
			    if (uR1 + 1 > 0xFF) { // Überlauf bei R1
			        // R2 erhöhen und prüfen, ob R2 überläuft
			        if (uR2 + 1 > 0xFF) {
			            // Überlauf bei R2: Beide Register auf 0xFF setzen
			            R1 = (byte) 0xFF;
			            R2 = (byte) 0xFF;
			        } else {
			            // Kein Überlauf bei R2: R2 erhöhen und R1 erhöhen
			            R2 = (byte) ((uR2 + 1) & 0xFF);
			            R1 = (byte) ((uR1 + 1) & 0xFF);
			        }
			    } else {
			        // Kein Überlauf bei R1: Nur R1 erhöhen
			        R1 = (byte) ((uR1 + 1) & 0xFF);
			    }
	
			    lastOpcode = 0x27;
			    lastOpcodeArgs = null;
			    lastOpcodeLen = 1;
			    lastAssembler = "IRW";
	
			    IC += 1;
			    break;
			    
			// DRW ($28, 1-Byte-OP): Erniedrigt den Wert von R1 um 1. Falls eine
			//    negative Zahl entsteht, enthält R1 dann den Betrag der negativen Zahl.
			//    Ferner wird dann R2 um 1 erniedrigt. Tritt dabei ein Unterlauf von R2 auf,
			//    werden R1 und R2 zu $00.
        	case 0x28:
        		uR1 = R1 & 0xFF;
        		
        		difference = uR1 - 1;
        		
        		// Falls Ergebnis negativ:
        		if (difference < 0) {
        			R1 = (byte) Math.abs(difference);
        			uR2 = R2 & 0xFF;
        			uR2 -= 1;
        			if (uR2 < 0) {
        				R1 = 0x00;
        				R2 = 0x00;
        			}
        			else {
        				R2 = (byte) (uR2 & 0xFF);
        			}
        		}
        		
        		lastOpcode = 0x28;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "DRW";
        		
        		IC += 1;
        		break;
        		
        	// X03 ($29, 1-Byte-OP): Vertauscht die Inhalte von R0 und R3.
        	case 0x29:
        		byte oldR0 = R0;
        		byte oldR3 = R3;
        		
        		R0 = oldR3;
        		R3 = oldR0;
        		
        		lastOpcode = 0x29;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "X03";
        		
        		IC += 1;
        		break;
        	
        	// C03 ($2A, 1-Byte-OP): Kopiert R0 nach R3.
        	case 0x2A:
        		R3 = R0;
        		lastOpcode = 0x2A;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "C03";
        		
        		IC += 1;
        		break;
        		
        	// C30 ($2B, 1-Byte-OP): Kopiert R3 nach R0.
        	case 0x2B:
        		R0 = R3;
        		lastOpcode = 0x2B;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "C30";
        		
        		IC += 1;
        		break;
        		
        	// PL0 ($2C, 1-Byte-OP): Schiebt die Bits in R0 um ein Bit nach
        	//	„links“ (entspricht Teilen ?MULTIPLIZIEREN? durch 2 ohne Rest)
        	case 0x2C:
        		R0 = (byte) (R0 << 1);
        		
        		lastOpcode = 0x2C;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "PL0";
        		
        		IC += 1;
        		break;
        		
        	// PR0 ($2D, 1-Byte-OP): Schiebt die Bits in R0 um ein Bit nach
        	//	„rechts“ (entspricht Multiplikation ?DIVISION? mit 2 ohne Übertrag).
        	case 0x2D:
        		R0 = (byte) (R0 >> 1);
        		
        		lastOpcode = 0x2D;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "PR0";
        		
        		IC += 1;
        		break;
        		
        	// HLT ($FF, 1-Byte-OP): Prozessor hält an.
        	case (byte) 0xFF:
        		
        		lastOpcode = (byte) 0xFF;
        		lastOpcodeArgs = null;
        		lastOpcodeLen = 1;
        		lastAssembler = "HLT";
        		
        		halt = true;
        		
        		break;
        			
            default:
                throw new IllegalArgumentException("Unbekannter Opcode: " + Integer.toHexString(opcode));
        }
    }

    // Hilfsmethoden zum Zugriff auf den Retro24-Speicher
    public void setIC(short value) {
        IC = value;
    }

    public int getIC() {
        return IC;
    }

    public void setAR(short value) {
        AR = value;
    }

    public int getAR() {
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
    
    public byte getLastOpcode() {
    	return lastOpcode;
    }
    
    public byte[] getLastOpcodeArgs() {
    	return lastOpcodeArgs;
    }
    
    public byte getlastOpcodeLen() {
    	return lastOpcodeLen;
    }
    
    public boolean istGestoppt() {
    	return halt;
    }
    
    public String getLastAssembler() {
    	return lastAssembler;
    }
}
