import static common.util.NumberUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.Retro24;
import core.CPU.CPU;

public class OpcodeTests {
	private Retro24 retro24;
	private CPU cpu;
	
	byte oldR0;
	byte oldR1;
	byte oldR2;
	byte oldR3;
	short oldIC;
	short oldAR;
	
	byte newR0;
	byte newR1;
	byte newR2;
	byte newR3;
	short newIC;
	short newAR;
	
	byte runnedOpcode;
	String runnedInstructionAssem;
	byte[] usedArgs;
	short operationLen;
	
	
	
	// OPCODES
	
	//OK
	@BeforeEach
	void setUp() {
		retro24 = new Retro24();
		retro24.initialize();
		cpu = retro24.getCPU();
		
		// Alte Werte zum späteren Vergleichen merken
		oldR0 = cpu.getR0();
		oldR1 = cpu.getR1();
		oldR2 = cpu.getR2();
		oldR3 = cpu.getR3();
		oldIC = (short) cpu.getIC();
		oldAR = (short) cpu.getAR();
	}
	
	@AfterEach
	// OK
	void after() {
	    assertTrue(cpu.getLastInstruction().getOpcode() == runnedOpcode);
	    assertTrue(Arrays.equals(cpu.getLastInstruction().getArgs(), usedArgs));
	    assertTrue(cpu.getLastInstruction().getAssemblerCode().equals(runnedInstructionAssem));
	    assertTrue(cpu.getIC() == trimToShort(addU(oldIC, operationLen)));
	}

	// Merken der neuen Registerwerte
	// OK
	void updateRegisterValues() {
		newR0 = cpu.getR0();
		newR1 = cpu.getR1();
		newR2 = cpu.getR2();
		newR3 = cpu.getR3();
		newIC = (short) cpu.getIC();
		newAR = (short) cpu.getAR();
	}
	
	// NUL ($00, 1-Byte-OP): Prozessor tut nichts
	// OK
	@Test
	void testNul() {
		runnedOpcode = 0x00;
		operationLen = 1;
		runnedInstructionAssem = "NUL";
		usedArgs = new byte[0];
		
		cpu.executeOpcode(runnedOpcode);
		
		updateRegisterValues();
		
		assertTrue(newR0 == oldR0);
		assertTrue(newR1 == oldR1);
		assertTrue(newR2 == oldR2);
		assertTrue(newR3 == oldR3);
		assertTrue(newAR == oldAR);
	}
	
	// MAR ($01, 3-Byte-OP): Lädt AR mit den nächsten beiden Bytes.
	// OK
	@Test
	void testMar() {
	    runnedOpcode = 0x01;
	    operationLen = 3;
	    runnedInstructionAssem = "MAR";
	    
	    // Simuliere die Argumente für den MAR-Befehl (zwei Bytes für AR im Speicher, LSB zuerst)
	    byte lowByte = 0x34; // LSB
	    byte highByte = 0x12; // MSB
	    usedArgs = new byte[]{lowByte, highByte};

	    // Lade die Bytes in den Speicher bei der aktuellen IC-Adresse (Little Endian)
	    retro24.writeMemory(addU(cpu.getIC(), 1), lowByte);
	    retro24.writeMemory(addU(cpu.getIC(), 2), highByte);

	    // Erwarteter Wert für AR nach Ausführung (Big Endian in Register)
	    short expectedAR = 0x1234;

	    // Führe den Opcode aus
	    cpu.executeOpcode(runnedOpcode);
	    
	    updateRegisterValues();

	    // Überprüfe, ob AR korrekt geladen wurde (Little Endian Speicher → Big Endian Register)
	    assertTrue(newAR == expectedAR);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}

	// SIC ($02, 1-Byte-OP): Speichert IC an die im AR angegebene Adresse.
	// OK
	@Test
	void testSic() {
	    runnedOpcode = 0x02;
	    operationLen = 1;
	    runnedInstructionAssem = "SIC";

	    // Simuliere die Argumente für den SIC-Befehl (keine Argumente für SIC)
	    usedArgs = new byte[0];

	    // Setze den Wert des AR auf eine Beispieladresse (z.B. 0x1234)
	    short testAR = 0x1234;
	    cpu.setAR(testAR);

	    // Führe den Opcode aus
	    cpu.executeOpcode(runnedOpcode);

	    updateRegisterValues();

	    // Überprüfe, ob der Wert von IC korrekt an die im AR angegebene Adresse im Speicher geschrieben wurde
	    byte lowByte = retro24.readMemory(testAR);      // Lese das niedrige Byte (LSB)
	    byte highByte = retro24.readMemory((addU(testAR,1)));  // Lese das hohe Byte (MSB)
	    
	    // Kombinieren der beiden Bytes zu einem 16-Bit Wert
	    short storedIC = twoByteToShort(highByte, lowByte);

	    assertTrue(storedIC == oldIC);


	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}
}
