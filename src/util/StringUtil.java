package util;

public class StringUtil {
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
}
