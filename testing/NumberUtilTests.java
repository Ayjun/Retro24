import org.junit.Test;

import static common.util.NumberUtil.*;
import static org.junit.Assert.assertEquals;

public class NumberUtilTests {
	@Test
	public void testCalculate1DAddress() {
		// 67 = 4. Pixel 2. Reihe (x=4, y=2)
		assertEquals(67, calculate1DAddress(4, 2, 64, 64, 0)); 
	}
}
