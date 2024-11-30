package util;

/**
 * Utility-Klasse für unsigned Operationen mit byte und short.
 * @author Eric Schneider
 */
public class NumberUtil {

    /**
     * Konvertiert einen signed short-Wert in einen unsigned int-Wert.
     * @param value der signed short-Wert
     * @return der unsigned int-Wert im Bereich 0 bis 65535
     */
    public static int unsign(short value) {
        return value & 0xFFFF;
    }

    /**
     * Konvertiert einen signed byte-Wert in einen unsigned int-Wert.
     * @param value der signed byte-Wert
     * @return der unsigned int-Wert im Bereich 0 bis 255
     */
    public static int unsign(byte value) {
        return value & 0xFF;
    }

    /**
     * Addiert zwei unsigned short-Werte und gibt das Ergebnis als int zurück.
     * @param a der erste unsigned short-Wert
     * @param b der zweite unsigned short-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(short a, short b) {
        return unsign(a) + unsign(b);
    }

    /**
     * Addiert zwei unsigned byte-Werte und gibt das Ergebnis als int zurück.
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(byte a, byte b) {
        return unsign(a) + unsign(b);
    }
    
    /**
     * Subtrahiert b von a und gibt das Ergebnis als int zurück.
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Differenz der unsigned Werte
     */
    public static int subU(byte a, byte b) {
    	return unsign(a) - unsign(b);
    }
    
    /**
     * 
     * @param highByte das höherwertige Byte
     * @param lowByte das niederwertige Byte
     * @return das short mit den ersten 8 Bit = high Byte und den zweiten 8 Bit = low Byte
     */
    public static short twoByteToShort(byte highByte, byte lowByte) {
    	return (short) (((highByte & 0xFF) << 8) | (lowByte & 0xFF));
    }
    
    /**
     * 
     * @param s das aufzuteilende short
     * @return byte array mit [0] = highByte und [1] = lowByte
     */
    public static byte[] shortToByteArray(short s) {
    	byte lowByte = (byte) (s & 0xFF);
    	byte highByte = (byte) ((s >>> 8) & 0xFF);
    	
    	return new byte[] {highByte, lowByte};
    }
}
