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
     * Addiert unsigned byte-Wert und unsigned short-Wert, gibt das Ergebnis als int zurück.
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(byte a, short b) {
        return unsign(a) + unsign(b);
    }
    
    /**
     * Addiert unsigned short-Wert und unsigned byte-Wert, gibt das Ergebnis als int zurück.
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(short a, byte b) {
        return unsign(a) + unsign(b);
    }
    
    /**
     * Addiert unsigned short-Wert und int-Wert, gibt das Ergebnis als int zurück.
     * Der int Wert kann negativ sein!
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(short a, int b) {
    	return unsign(a) + b;
    }
    
    /**
     * Addiert unsigned byte-Wert und int-Wert, gibt das Ergebnis als int zurück.
     * Der int Wert kann negativ sein!
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Summe der unsigned Werte
     */
    public static int addU(byte a, int b) {
    	return unsign(a) + b;
    }
    
    /**
     * Subtrahiert unsigned b von unsigned a und gibt das Ergebnis als int zurück.
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Differenz der unsigned Werte
     */
    public static int subU(byte a, byte b) {
    	return unsign(a) - unsign(b);
    }
    
    /**
     * Subtrahiert b von unsigned a und gibt das Ergebnis als int zurück.
     * b kann negativ sein!
     * @param a der erste unsigned byte-Wert
     * @param b der zweite unsigned byte-Wert
     * @return die Differenz der unsigned Werte
     */
    public static int subU(byte a, int b) {
    	return unsign(a) - b;
    }
    
    /**
     * Kürzt ein int zu einem byte
     * @return den abgeschnittenen byte Wert des übergebenen int
     */
    public static byte trimToByte(int toTrim) {
    	return (byte) (toTrim & 0xFF);
    }
    
    /**
     * Kürzt ein int zu einem short
     * @return den abgeschnittenen byte Wert des übergebenen int
     */
    public static short trimToShort(int toTrim) {
    	return (short) (toTrim & 0xFFFF);
    }
    
    /**
     * Prüft ein int darauf, ob es größer als ein unsigned Byte ist (Overflow).
     * @return true wenn Overflow, false wenn nicht
     */
    public static boolean checkByteOverflow(int toCheck) {
    	boolean carry = false;   	
    	if (toCheck > 0xFF) {
    		carry = true;
    	}
    	return carry;
    }
    
    /**
     * Prüft ein int darauf, ob es kleiner als 0 ist (Underflow).
     * @return true wenn Underflow, false wenn nicht
     */
    public static boolean checkUnderflow(int toCheck) {
    	return toCheck < 0;
    }
    
    /**
     * Prüft ein int darauf, ob es größer als ein unsigned short ist (Overflow).
     * @return true wenn Overflow, false wenn nicht
     */
    public static boolean checkShortOverflow(int toCheck) {
    	boolean carry = false;   	
    	if (toCheck > 0xFFFF) {
    		carry = true;
    	}
    	return carry;
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
    
    /**
     * Bildet das unsigned logische Oder aus a und b und gibt das Ergebnis als int zurück.
     * @param a
     * @param b
     * @return
     */
    public static int uOr(byte a, byte b) {
    	return unsign(a) | unsign(b);
    }
    
    /**
     * Bildet das unsigned logische Und aus a und b und gibt das Ergebnis als int zurück.
     * @param a
     * @param b
     * @return
     */
    public static int uAnd(byte a, byte b) {
    	return unsign(a) & unsign(b);
    }
    
    /**
     * Methode ermittelt die Position einer zweidimensionalen Koordinate 
     * in einem eindimensionalen Raum.
     * @param x die horizontale Koordinate der zu ermitteltenden Position
     * @param y die vertikale Koordinate der zu ermitteltenden Position
     * @param xSize die Breite des zweidimensionalen Raumes
     * @param ySize die Höhe des zweidimensionalen Raumes
     * @param startAdress die Startaddresse (offset) für die Position im eindimensionalen Raum
     * @return die Position im eindimensionalen Raum
     */
    public static int calculate1DAddress(int x, int y, int xSize, int ySize, int startAdress) {
        // Überprüfen, ob die Koordinaten innerhalb der 1-basierten Grenzen liegen
        if (x < 1 || x > xSize || y < 1 || y > ySize) {
            throw new IllegalArgumentException("Koordinaten sind außerhalb des gültigen Bereichs.");
        }
        // Anpassen der 1-basierten Koordinaten auf 0-basierten Index
        int adjustedX = x - 1;
        int adjustedY = y - 1;
        
        // Berechnung der eindimensionalen Position
        return startAdress + adjustedY * xSize + adjustedX;
    }
    
    /**
     * Konvertiert ein boolean zu eine Byte bei dem das letzte Bit entsprechend des booleans gesetzt ist.
     * (false = 0x00, true = 0x01)
     * @param bool der boolean
     * @return das byte
     */
    public static byte booleanToByte(boolean bool) {
    	return (byte) (bool ? 1 : 0);
    }
    
    public static int[][] calcHorizontalLine(int xFrom, int xTo, int y) {
        // Berechnen der Anzahl der Punkte in der Linie
        int length = Math.abs(xTo - xFrom) + 1;
        
        // 2D-Array initialisieren
        int[][] line = new int[length][2];
        
        // Punkte zur Linie hinzufügen
        int index = 0;
        if (xFrom <= xTo) {
            for (int x = xFrom; x <= xTo; x++) {
                line[index][0] = x; // x variiert
                line[index][1] = y; // y bleibt konstant
                index++;
            }
        } else {
            for (int x = xFrom; x >= xTo; x--) {
                line[index][0] = x; // x variiert abwärts
                line[index][1] = y; // y bleibt konstant
                index++;
            }
        }
        
        return line;
    }
    
    public static int[][] calcVerticalLine(int yFrom, int yTo, int x) {
        // Berechnen der Anzahl der Punkte in der Linie
        int length = Math.abs(yTo - yFrom) + 1;
        
        // 2D-Array initialisieren
        int[][] line = new int[length][2];
        
        // Punkte zur Linie hinzufügen
        int index = 0;
        if (yFrom <= yTo) {
            for (int y = yFrom; y <= yTo; y++) {
                line[index][0] = x; // x bleibt konstant
                line[index][1] = y; // y variiert
                index++;
            }
        } else {
            for (int y = yFrom; y >= yTo; y--) {
                line[index][0] = x; // x bleibt konstant
                line[index][1] = y; // y variiert abwärts
                index++;
            }
        }
        
        return line;
    }

}
