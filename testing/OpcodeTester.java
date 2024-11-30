import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.Retro24;
import core.CPU.CPU;

public class OpcodeTester {
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

	// Merken der neuen Registerwerte
	void updateRegisterValues() {
		newR0 = cpu.getR0();
		newR1 = cpu.getR1();
		newR2 = cpu.getR2();
		newR3 = cpu.getR3();
		newIC = (short) cpu.getIC();
		newAR = (short) cpu.getAR();
	}
	
	
	// NUL ($00, 1-Byte-OP): Prozessor tut nichts
	@Test
	void testNul() {
		byte opcode = 0x00;
		byte len = 1;
		String assembler = "NUL";
		
		cpu.executeOpcode(opcode);
		
		updateRegisterValues();
		
		assertTrue(newR0 == oldR0);
		assertTrue(newR1 == oldR1);
		assertTrue(newR2 == oldR2);
		assertTrue(newR3 == oldR3);
		assertTrue(newIC == oldIC + len);
		assertTrue(newAR == oldAR);
	}
	
	// MAR ($01, 3-Byte-OP): Lädt AR mit den nächsten beiden Bytes.
	@Test
	void testMar() {
	    byte opcode = 0x01;
	    byte len = 3;
	    String assembler = "MAR";
	    
	    // Simuliere die Argumente für den MAR-Befehl (zwei Bytes für AR im Speicher, LSB zuerst)
	    byte lowByte = 0x34; // LSB
	    byte highByte = 0x12; // MSB
	    byte[] opcodeArgs = {lowByte, highByte};

	    // Lade die Bytes in den Speicher bei der aktuellen IC-Adresse (Little Endian)
	    retro24.writeMemory(cpu.getIC() + 1, lowByte);
	    retro24.writeMemory(cpu.getIC() + 2, highByte);

	    // Erwarteter Wert für AR nach Ausführung (Big Endian in Register)
	    short expectedAR = (short) (((highByte & 0xFF) << 8) | (lowByte & 0xFF));

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);
	    
	    updateRegisterValues();

	    // Überprüfe, ob AR korrekt geladen wurde (Little Endian Speicher → Big Endian Register)
	    assertTrue(newAR == expectedAR);
	    

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}
	
	// SIC ($02, 1-Byte-OP): Speichert IC an die im AR angegebene Adresse.
	@Test
	void testSic() {
	    byte opcode = 0x02;
	    byte len = 1;
	    String assembler = "SIC";

	    // Simuliere die Argumente für den SIC-Befehl (keine Argumente für SIC, daher null)
	    byte[] opcodeArgs = null;

	    // Setze den Wert des AR auf eine Beispieladresse (z.B. 0x1234)
	    short testAR = 0x1234;
	    cpu.setAR(testAR);

	    // Speichere den Wert des IC, den wir später überprüfen möchten
	    short icBefore = (short) cpu.getIC();

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob der Wert von IC korrekt an die im AR angegebene Adresse im Speicher geschrieben wurde
	    byte lowByte = retro24.readMemory(testAR);      // Lese das niedrige Byte (LSB)
	    byte highByte = retro24.readMemory((short) (testAR + 1));  // Lese das hohe Byte (MSB)
	    
	    // Der erwartete Wert im Speicher ist der Wert von IC
	    short expectedIC = icBefore;

	    // Kombinieren der beiden Bytes zu einem 16-Bit Wert
	    short storedIC = (short) (((highByte & 0xFF) << 8) | (lowByte & 0xFF));

	    assertTrue(storedIC == expectedIC);

	    // Überprüfe, ob IC korrekt erhöht wurde (obwohl SIC den IC nicht ändert, sondern nur den Speicher schreibt)
	    assertTrue(newIC == oldIC + len);


	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}

	// Ich gehe davon aus es ist gemeint R1 gefolgt von R2 sollen der Inhalt fuer AR werden?
	// Weiterhin gehe ich davon aus dass R1 das höherwertige Byte sein soll?
	// RAR ($03, 1-Byte-OP): R1/R2 werden ins AR kopiert.
	@Test
	void testRar() {
	    byte opcode = 0x03;
	    byte len = 1;
	    String assembler = "RAR";

	    // Simuliere die Argumente für den RAR-Befehl (keine Argumente für RAR, daher null)
	    byte[] opcodeArgs = null;

	    // Setze die Werte von R1 und R2, die ins AR kopiert werden sollen
	    byte r1Value = 0x12; // Beispielwert für R1 (höheres Byte)
	    byte r2Value = 0x34; // Beispielwert für R2 (niedrigeres Byte)
	    cpu.setR1(r1Value);
	    cpu.setR2(r2Value);

	    // Erwarteter Wert für AR nach Ausführung (R1 wird das hohe Byte, R2 das niedrige Byte)
	    short expectedAR = (short) (((r1Value & 0xFF) << 8) | (r2Value & 0xFF));

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob AR korrekt gesetzt wurde (R1 wird das hohe Byte, R2 das niedrige Byte)
	    assertTrue(newAR == expectedAR);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR3 == oldR3);
	}

/*
	// AAR ($04, 1-Byte-OP): Addiert R0 aufs AR, bei Überlauf geht Übertrag verloren.
	@Test
	void testAarWithOverflow() {
	    byte opcode = 0x04;
	    byte len = 1;
	    String assembler = "AAR";

	    // Simuliere die Argumente für den AAR-Befehl (keine Argumente für AAR, daher null)
	    byte[] opcodeArgs = null;

	    // Setze den Wert von AR und R0 für den Test
	    short initialAR = (short) 0xFFFF; // Maximaler Wert für AR (16-Bit-Grenze)
	    byte r0Value = 0x01; // Beispielwert für R0, der einen Überlauf verursachen soll

	    cpu.setAR(initialAR); // AR auf den maximalen Wert setzen
	    cpu.setR0(r0Value);  // R0 auf den Wert 0x01 setzen

	    // Erwarteter Wert für AR nach der Addition (AR + R0)
	    // Das Ergebnis soll 0x10000 sein, aber wir erwarten nur die unteren 16 Bits (also 0x0000)
	    short expectedAR = (short) ((initialAR + (r0Value & 0xFF)) & 0xFFFF);  // Nur die unteren 16 Bits berücksichtigen

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob AR korrekt gesetzt wurde (Überlauf berücksichtigt)
	    assertTrue(newAR == expectedAR);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, dass R0 immer noch den richtigen Wert hat (in diesem Test gesetzt)
	    assertTrue(newR0 == r0Value);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}

	// IR0 ($05, 1-Byte-OP): Erhöht den Wert von R0 um 1, allerdings nicht über $FF hinaus.
	@Test
	void testIr0IncreaseR0() {
	    byte opcode = 0x05;
	    byte len = 1;
	    String assembler = "IR0";

	    // Simuliere die Argumente für den IR0-Befehl (keine Argumente für IR0, daher null)
	    byte[] opcodeArgs = null;

	    // Test 1: Wenn R0 kleiner als 0xFF ist, soll es um 1 erhöht werden
	    byte initialR0 = 0x10;  // Beispielwert unter 0xFF
	    byte expectedR0 = (byte) (initialR0 + 1);  // Erwarteter Wert von R0

	    cpu.setR0(initialR0);  // Setze den Startwert für R0

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R0 korrekt erhöht wurde
	    assertTrue(newR0 == expectedR0);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}

	// IR0 ($05, 1-Byte-OP): Erhöht den Wert von R0 um 1, allerdings nicht über $FF hinaus.
	// (FALL ÜBERLAUF)
	@Test
	void testIr0NoOverflow() {
	    byte opcode = 0x05;
	    byte len = 1;
	    String assembler = "IR0";

	    // Simuliere die Argumente für den IR0-Befehl (keine Argumente für IR0, daher null)
	    byte[] opcodeArgs = null;

	    // Test 2: Wenn R0 bereits 0xFF ist, soll R0 unverändert bleiben
	    byte initialR0 = (byte) 0xFF;  // Setze R0 auf den maximalen Wert
	    byte expectedR0 = (byte) 0xFF; // Erwartet, dass R0 nach der Ausführung immer noch 0xFF bleibt

	    cpu.setR0(initialR0);  // Setze den Startwert für R0

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R0 korrekt bleibt (kein Überlauf)
	    assertTrue(newR0 == expectedR0);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}

	// A01 ($06, 1-Byte-OP): Addiert R0 auf R1. Bei Überlauf wird R2 um 1 erhöht. Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
	@Test
	void testA01NoOverflow() {
	    byte opcode = 0x06;
	    byte len = 1;
	    String assembler = "A01";

	    // Simuliere die Argumente für den A01-Befehl (keine Argumente für A01, daher null)
	    byte[] opcodeArgs = null;

	    // Test 1: Kein Überlauf, R1 + R0 bleibt <= 0xFF
	    byte initialR0 = 0x10; // Beispielwert
	    byte initialR1 = 0x20; // Beispielwert
	    byte initialR2 = 0x00; // Kein Überlauf in R2
	    byte expectedR1 = (byte) (initialR1 + initialR0); // Erwarteter Wert in R1
	    byte expectedR2 = initialR2; // R2 bleibt unverändert

	    cpu.setR0(initialR0);  // Setze den Startwert für R0
	    cpu.setR1(initialR1);  // Setze den Startwert für R1
	    cpu.setR2(initialR2);  // Setze den Startwert für R2

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R1 korrekt erhöht wurde (kein Überlauf)
	    assertTrue(newR1 == expectedR1);

	    // Überprüfe, ob R2 unverändert blieb
	    assertTrue(newR2 == expectedR2);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == initialR0);
	    assertTrue(newR3 == oldR3);
	}
	
	// A01 ($06, 1-Byte-OP): Addiert R0 auf R1. Bei Überlauf wird R2 um 1 erhöht. Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
	// ÜBERLAUF 1
	@Test
	void testA01WithOverflowR2() {
	    byte opcode = 0x06;
	    byte len = 1;
	    String assembler = "A01";

	    // Simuliere die Argumente für den A01-Befehl (keine Argumente für A01, daher null)
	    byte[] opcodeArgs = null;

	    // Test 2: Überlauf, R1 + R0 > 0xFF, also R2 wird um 1 erhöht
	    byte initialR0 = 0x10; // Beispielwert
	    byte initialR1 = (byte) 0xF0; // R1 + R0 wird > 0xFF
	    byte initialR2 = 0x00; // Kein Überlauf in R2
	    byte expectedR1 = (byte) (initialR1 + initialR0); // Erwarteter Wert in R1
	    byte expectedR2 = 0x01; // R2 wird um 1 erhöht

	    cpu.setR0(initialR0);  // Setze den Startwert für R0
	    cpu.setR1(initialR1);  // Setze den Startwert für R1
	    cpu.setR2(initialR2);  // Setze den Startwert für R2

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R1 korrekt erhöht wurde (Überlauf in R1)
	    assertTrue(newR1 == expectedR1);

	    // Überprüfe, ob R2 korrekt erhöht wurde
	    assertTrue(newR2 == expectedR2);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == initialR0);
	    assertTrue(newR3 == oldR3);
	}
	
	// A01 ($06, 1-Byte-OP): Addiert R0 auf R1. Bei Überlauf wird R2 um 1 erhöht. Läuft dabei wiederum R2 über, werden R1 und R2 zu $FF.
	// ÜBERLAUF 2
	@Test
	void testA01WithOverflowR2AndR1() {
	    byte opcode = 0x06;
	    byte len = 1;
	    String assembler = "A01";

	    // Simuliere die Argumente für den A01-Befehl (keine Argumente für A01, daher null)
	    byte[] opcodeArgs = null;

	    // Test 3: Überlauf in R1 und R2, R2 geht über 0xFF, also R1 und R2 werden zu 0xFF
	    byte initialR0 = 0x10; // Beispielwert
	    byte initialR1 = (byte) 0xF0; // R1 + R0 wird > 0xFF
	    byte initialR2 = (byte) 0xFF; // R2 ist schon 0xFF, und wird beim Überlauf nicht weiter erhöht
	    byte expectedR1 = (byte) 0xFF; // R1 wird zu 0xFF
	    byte expectedR2 = (byte) 0xFF; // R2 wird zu 0xFF

	    cpu.setR0(initialR0);  // Setze den Startwert für R0
	    cpu.setR1(initialR1);  // Setze den Startwert für R1
	    cpu.setR2(initialR2);  // Setze den Startwert für R2

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R1 korrekt zu 0xFF gesetzt wurde
	    assertTrue(newR1 == expectedR1);

	    // Überprüfe, ob R2 korrekt zu 0xFF gesetzt wurde
	    assertTrue(newR2 == expectedR2);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob die Hilfsvariablen korrekt gesetzt wurden
	    testHelperVars(opcode, opcodeArgs, len, assembler);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == initialR0);
	    assertTrue(newR3 == oldR3);
	}

	// DR0 ($07, 1-Byte-OP): Erniedrigt den Wert von R0 um 1, allerdings nicht
	// unter $00.
	@Test
	void testDR0() {
	    byte opcode = 0x07;
	    byte len = 1;
	    String assembler = "DR0";

	    // Test 1: R0 wird von 0x01 auf 0x00 verringert
	    byte initialR0 = 0x01;  // Setze R0 auf 0x01
	    byte expectedR0 = 0x00;  // Erwartet, dass R0 auf 0x00 sinkt

	    cpu.setR0(initialR0);    // Setze den Startwert für R0

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R0 korrekt verringert wurde (von 0x01 auf 0x00)
	    assertTrue(newR0 == expectedR0);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);

	    // Test 2: R0 bleibt bei 0x00
	    initialR0 = 0x00;  // Setze R0 auf 0x00
	    expectedR0 = 0x00; // Erwartet, dass R0 weiterhin 0x00 bleibt

	    cpu.setR0(initialR0);    // Setze den Startwert für R0

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R0 korrekt bleibt (nicht unter 0x00 gefallen)
	    assertTrue(newR0 == expectedR0);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + 2 * len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR1 == oldR1);
	    assertTrue(newR2 == oldR2);
	    assertTrue(newR3 == oldR3);
	}
	
	// S01 ($08, 1-Byte-OP): Subtrahiert R0 von R1. Falls eine negative Zahl
	// entsteht, enthält R1 dann den Betrag der negativen Zahl. Ferner wird dann
	// R2 um 1 erniedrigt. Tritt dabei ein Unterlauf von R2 auf, werden R1 und R2
	// zu $00.
	@Test
	void testS01() {
	    byte opcode = 0x08;
	    byte len = 1;
	    String assembler = "S01";

	    // Test 1: Subtraktion führt zu negativem Wert
	    byte initialR1 = 0x10;  // R1 = 0x10
	    byte initialR0 = 0x20;  // R0 = 0x20
	    byte expectedR1 = 0x10; // R1 soll 0x10 sein (Betrag der negativen Zahl)
	    byte expectedR2 = (byte) 0xFF; // R2 soll um 1 verringert werden

	    cpu.setR1(initialR1);
	    cpu.setR0(initialR0);
	    cpu.setR2((byte) 0x01); // R2 initialisieren

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R1 den Betrag der negativen Zahl enthält
	    assertTrue(newR1 == expectedR1);
	    // Überprüfe, ob R2 um 1 verringert wurde
	    System.out.println(newR2 + " " + expectedR2);
	    assertTrue(newR2 == expectedR2);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR3 == oldR3);

	    // Test 2: Subtraktion führt zu Unterlauf in R2
	    initialR1 = 0x10;  // R1 = 0x10
	    initialR0 = 0x20;  // R0 = 0x20
	    expectedR1 = 0x00; // R1 wird auf 0x00 gesetzt
	    expectedR2 = 0x00; // R2 wird auf 0x00 gesetzt

	    cpu.setR1(initialR1);
	    cpu.setR0(initialR0);
	    cpu.setR2((byte) 0x00); // R2 = 0x00, führt zu einem Unterlauf

	    // Führe den Opcode aus
	    cpu.executeOpcode(opcode);

	    updateRegisterValues();

	    // Überprüfe, ob R1 und R2 auf 0x00 gesetzt wurden
	    assertTrue(newR1 == expectedR1);
	    assertTrue(newR2 == expectedR2);

	    // Überprüfe, ob IC korrekt erhöht wurde
	    assertTrue(newIC == oldIC + len);

	    // Überprüfe, ob keine anderen Register verändert wurden
	    assertTrue(newR0 == oldR0);
	    assertTrue(newR3 == oldR3);
	}

	
*/
	
	
	
	
	
	
	
}
