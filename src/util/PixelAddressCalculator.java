package util;
import static util.NumberUtil.*;
import static core.graphics.GraphicChip.*;

import java.util.Scanner;

public class PixelAddressCalculator {
	public static void main(String[] args) {
		while(true) {
			Scanner scan = new Scanner(System.in);
			String input = scan.nextLine();
			int x = Integer.parseInt(input.split(",")[0]);
			int y = Integer.parseInt(input.split(",")[1]);
			int addr1 = calculate1DAddress(x, y, PIXELWIDTH, PIXELHEIGHT,VIDMEMDARKLIGHTSTART);
			int addr2 =calculate1DAddress(x, y, PIXELWIDTH, PIXELHEIGHT,VIDMEMMONOCHROMCOLORSTART);
			System.out.println("Light/dark: " + String.format("0x%02X", addr1));
			System.out.println("Color/monocrhome: " + String.format("0x%02X", addr2));
		}
	}
}
