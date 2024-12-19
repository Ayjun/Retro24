package common.util;
import static common.util.NumberUtil.*;
import static core.graphics.GraphicChip.*;

import java.util.Scanner;

public class PixelAddressCalculator {
	public static void main(String[] args) {
		while(true) {
			Scanner scan = new Scanner(System.in);
			String input = scan.nextLine();
			int x = Integer.parseInt(input.split(",")[0]);
			int y = Integer.parseInt(input.split(",")[1]);
			int addr1 = calculate1DAddress(x, y, PIXEL_WIDTH, PIXEL_HEIGHT,VIDMEM_DARK_LIGHT_START);
			int addr2 =calculate1DAddress(x, y, PIXEL_WIDTH, PIXEL_HEIGHT,VIDMEM_MONOCHROM_COLOR_START);
			System.out.println("Light/dark: " + String.format("0x%02X", addr1));
			System.out.println("Color/monocrhome: " + String.format("0x%02X", addr2));
		}
	}
}
