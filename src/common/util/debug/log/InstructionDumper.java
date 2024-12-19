package common.util.debug.log;

import static common.util.StringUtil.byteArrayToString;

import common.config.InstructionInfoConfig;
import core.Retro24;

/**
 * Erstellt formatierten Instruction Dump aus dem Retro24 CPU und seinen Registern.
 */
public class InstructionDumper extends Dumper {
    
    public InstructionDumper(Retro24 retro24, InstructionInfoConfig config) {
    	super(retro24, config);
    }
    
    /**
     * @Override
     * Erstellt einen formatierten Instruction Dump des aktuellen Prozessorzustands.
     * 
     * @return Formatierter Instruction Dump als String oder null wenn Dumps deaktiviert sind
     */
    public String dump() {
    	if (!config.isEnabled()) {
            return null;
        }
    	
    	StringBuilder dump = new StringBuilder();
    	
    	dump.append("##  " + retro24.getCPU().getLastInstruction())
    	.append(" ")
    	.append(byteArrayToString(retro24.getCPU().getLastInstruction().getArgs()))
    	.append(System.lineSeparator())
    	.append("Registerinhalt:")
		.append(System.lineSeparator())
		.append("R0: " + String.format("0x%02X", retro24.getCPU().getR0()))
		.append(System.lineSeparator())
		.append("R1: " + String.format("0x%02X", retro24.getCPU().getR1()))
		.append(System.lineSeparator())
		.append("R2: " + String.format("0x%02X", retro24.getCPU().getR2()))
		.append(System.lineSeparator())
		.append("R3: " + String.format("0x%02X", retro24.getCPU().getR3()))
		.append(System.lineSeparator())
		.append("IC: " + String.format("0x%04X", retro24.getCPU().getIC()))
		.append(System.lineSeparator())
		.append("AR: " + String.format("0x%04X", retro24.getCPU().getAR()))
		.append(System.lineSeparator())
		.append("########################")
		.append(System.lineSeparator());
    	
		return dump.toString();
    }
}
