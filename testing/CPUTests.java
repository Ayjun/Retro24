import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Retro24;
import core.CPU.CPU;

public class CPUTests {
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
	
	@Test
	public void testICWrapAround() {
		cpu.moveIC(0xFFFF);
		assertEquals(cpu.getIC(), 0x00FF);
	}
}
