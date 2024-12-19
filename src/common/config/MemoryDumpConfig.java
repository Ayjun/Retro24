package common.config;

/**
 * Stellt die Konfiguration für einen Memory Dump dar.
 * @author Eric Schneider
 */
public class MemoryDumpConfig extends Config{

	private int startAddress;
	private int endAddress;
	
	/**
	 * Erstellt eine neue MemoryDumpConfig
	 * Es findet hier keine Validierung der Gültigkeit der Adressen statt!
	 * @param enabled <- ist MemoryDump aktiv?
	 * @param startAddress <- Startadresse des Dumpbereichs
	 * @param endAddress <- Endaddresse des Dumpbereichs
	 */
	public MemoryDumpConfig(boolean enabled, int startAddress, int endAddress) {
		super(enabled);
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public int getEndAddress() {
		return endAddress;
	}

}
