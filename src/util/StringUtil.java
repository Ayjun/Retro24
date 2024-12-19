package util;

import java.math.BigInteger;

public class StringUtil {
	/**
	 * Gibt ein Byte Array als String in hexadezimaler Schreibweise zurück
	 * @param a das byte Array
	 * @return
	 */
	public static String byteArrayToString(byte[] a) {
		String str = "";
		for (int i=0; i < a.length; i++) {
			if (i > 0 && i % 8 == 0 && a.length > i+1) {
				str += System.lineSeparator();
			}
			str += (String.format("0x%02X", a[i]) + " ");
		}
		return str;
	}
	
	/**
	 * Wandelt eine hexadezimale Zahl in String Schreibweise in einen int um.
	 * @param String in der Form "0x1234"
	 * @return int Wert des übergebenen hexStrings
	 */
	public static int hexStringToInt(String hexString) throws IllegalArgumentException {
		if (!hexString.matches("^0[xX][0-9a-fA-F]+")) {
			throw new IllegalArgumentException("Falsches Format für eine hexadezimale Zahl: " + hexString);
		}
		
		// Verwende BigInteger zur Validierung der Größenbeschränkung
	    BigInteger value = new BigInteger(hexString.substring(2), 16);

	    // Überprüfe, ob der Wert in den int-Bereich passt
	    if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 || 
	        value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
	        throw new IllegalArgumentException("Wert außerhalb des gültigen int-Bereichs: " + hexString);
	    }

	    // Rückgabe als int
	    return value.intValue();
	}
	
	/**
	 * Wandelt ein int zu einem hexadezimal String der Form 0x1234
	 * @param i int
	 * @return die hexadezimale String Repäsentation von i
	 */
	public static String intToHexString(int i) {
		return String.format("0x%02X", i);
	}
}
