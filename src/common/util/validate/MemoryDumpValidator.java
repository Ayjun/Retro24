package common.util.validate;

/**
 * Prüft Speicheradressen auf Gültigkeit im Kontext von einem Retro24.
 */
public class MemoryDumpValidator {
    private final int memStart;
    private final int memEnd;
    private final int maxRange;

    /**
     * Erstellt einen neuen MemoryDumpValidator.
     * 
     * @param memStart Startadresse des gültigen Speicherbereichs
     * @param memEnd Endadresse des gültigen Speicherbereichs
     * @param maxRange Maximale Größe eines Speicherbereichs für Dumps
     * @throws IllegalArgumentException wenn memStart > memEnd oder maxRange < 0
     */
    public MemoryDumpValidator(int memStart, int memEnd, int maxRange) {
        if (memStart > memEnd) {
            throw new IllegalArgumentException("memStart kann nicht größer als memEnd sein");
        }
        if (maxRange < 0) {
            throw new IllegalArgumentException("maxRange kann nicht negativ sein");
        }
        
        this.memStart = memStart;
        this.memEnd = memEnd;
        this.maxRange = maxRange;
    }

    /**
     * Validiert ob die Adresse innerhalb des gültigen Speicherbereiches liegt.
     * 
     * @param address zu prüfende Speicheradresse
     * @return true wenn die Adresse gültig ist, sonst false
     */
    public boolean validateAddress(int address) {
        return address >= memStart && address <= memEnd;
    }

    /**
     * Validiert ob eine Range zwischen 2 Adressen gültig ist.
     * Eine Range ist gültig wenn:
     * - Start- und Endadresse im gültigen Speicherbereich liegen
     * - Startadresse kleiner oder gleich der Endadresse ist
     * - Die Range nicht größer als maxRange ist
     * 
     * @param start Startadresse des zu prüfenden Bereichs
     * @param end Endadresse des zu prüfenden Bereichs
     * @return true wenn die Range gültig ist, sonst false
     */
    public boolean validateAddressRange(int start, int end) {
        if (!validateAddress(start) || !validateAddress(end)) {
            return false;
        }
        
        if (start > end) {
            return false;
        }

        return (end - start) <= maxRange;
    }

    /**
     * Gibt die maximale erlaubte Größe eines Speicherbereichs zurück.
     */
    public int getMaxRange() {
        return maxRange;
    }

    /**
     * Gibt die Startadresse des gültigen Speicherbereichs zurück.
     */
    public int getMemStart() {
        return memStart;
    }

    /**
     * Gibt die Endadresse des gültigen Speicherbereichs zurück.
     */
    public int getMemEnd() {
        return memEnd;
    }
}