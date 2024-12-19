package common.util.debug.log;

import common.config.MemoryDumpConfig;
import core.Retro24;
import static common.util.StringUtil.*;

/**
 * Erstellt formatierte Memory Dumps aus dem Retro24 Speicher.
 */
public class MemoryDumper extends Dumper {
    
    private static final int BYTES_PER_LINE = 16;

    public MemoryDumper(Retro24 retro24, MemoryDumpConfig config) {
    	super(retro24, config);
    }

    /**
     * @Override
     * Erstellt einen formatierten Memory Dump des aktuellen Speicherzustands.
     * 
     * @return Formatierter Memory Dump als String oder null wenn Dumps deaktiviert sind
     */
    public String dump() {
        if (!config.isEnabled()) {
            return null;
        }

        int startAddress = ((MemoryDumpConfig) config).getStartAddress();
        int endAddress = ((MemoryDumpConfig) config).getEndAddress();

        StringBuilder dump = new StringBuilder();
        appendHeader(dump, startAddress, endAddress);
        appendMemoryContent(dump, startAddress, endAddress);
        
        return dump.toString();
    }

    private void appendHeader(StringBuilder dump, int startAddress, int endAddress) {
    	dump.append("### Speicherauszug von ")
        .append(String.format("0x%04X", startAddress))
        .append(" bis ")
        .append(String.format("0x%04X", endAddress))
        .append(":")
        .append(System.lineSeparator());
    }

    private void appendMemoryContent(StringBuilder dump, int startAddress, int endAddress) {
    	for (int address = startAddress; address <= endAddress; address += BYTES_PER_LINE) {
	        dump.append(String.format("0x%04X: ", address));

	        for (int offset = 0; offset < BYTES_PER_LINE && (address + offset) <= endAddress; offset++) {
	            byte value = retro24.readMemory((short) (address + offset));
	            dump.append(String.format("%02X ", value));
	        }
	        dump.append(System.lineSeparator());
        }
    	dump.append(System.lineSeparator());
    }
}