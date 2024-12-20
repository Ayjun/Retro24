package core.graphics;

public class StartScreenLoader {
	
	private final GraphicChip graphicChip;
	
	public StartScreenLoader(GraphicChip graphicChip) {
		this.graphicChip = graphicChip;
	}
	
	/**
	 * Schreibt den RETRO24 Screen in den Videospeicher :)
	 */
	public void loadRetro24WelcomeScreen() {
		int[][] yellowCoordinates = {
	            {6, 16}, {7, 16}, {8, 16}, {9, 16}, {10, 16}, {11, 16}, {12, 16}, {13, 16},
	            {14, 16}, {15, 16}, {16, 16}, {17, 16}, {18, 16}, {19, 16}, {20, 16}, {21, 16},
	            {22, 16}, {23, 16}, {24, 16}, {25, 16}, {26, 16}, {27, 16}, {28, 16}, {29, 16},
	            {30, 16}, {31, 16}, {32, 16}, {33, 16}, {34, 16}, {35, 16}, {36, 16}, {37, 16},
	            {38, 16}, {39, 16}, {40, 16}, {41, 16}, {42, 16}, {43, 16}, {44, 16}, {45, 16},
	            {46, 16}, {47, 16}, {48, 16}, {49, 16}, {50, 16}, {51, 16}, {52, 16}, {53, 16},
	            {54, 16}, {55, 16}, {56, 16}, {57, 16}, {58, 16}, {6, 39}, {7, 39}, {8, 39},
	            {9, 39}, {10, 39}, {11, 39}, {12, 39}, {13, 39}, {14, 39}, {15, 39}, {16, 39},
	            {17, 39}, {18, 39}, {19, 39}, {20, 39}, {21, 39}, {22, 39}, {23, 39}, {24, 39},
	            {25, 39}, {26, 39}, {27, 39}, {28, 39}, {29, 39}, {30, 39}, {31, 39}, {32, 39},
	            {33, 39}, {34, 39}, {35, 39}, {36, 39}, {37, 39}, {38, 39}, {39, 39}, {40, 39},
	            {41, 39}, {42, 39}, {43, 39}, {44, 39}, {45, 39}, {46, 39}, {47, 39}, {48, 39},
	            {49, 39}, {50, 39}, {51, 39}, {52, 39}, {53, 39}, {54, 39}, {55, 39}, {56, 39},
	            {57, 39}, {58, 39}
	        };
		
		int[][] whiteCoordinates = {
	            {5, 17}, {6, 17}, {7, 17}, {8, 17}, {9, 17}, {10, 17}, {11, 17}, {12, 17},
	            {13, 17}, {14, 17}, {15, 17}, {16, 17}, {17, 17}, {18, 17}, {19, 17}, {20, 17},
	            {21, 17}, {22, 17}, {23, 17}, {24, 17}, {25, 17}, {26, 17}, {27, 17}, {28, 17},
	            {29, 17}, {30, 17}, {31, 17}, {32, 17}, {33, 17}, {34, 17}, {35, 17}, {36, 17},
	            {37, 17}, {38, 17}, {39, 17}, {40, 17}, {41, 17}, {42, 17}, {43, 17}, {44, 17},
	            {45, 17}, {46, 17}, {47, 17}, {48, 17}, {49, 17}, {50, 17}, {51, 17}, {52, 17},
	            {53, 17}, {54, 17}, {55, 17}, {56, 17}, {57, 17}, {58, 17}, {59, 17}, {5, 38},
	            {6, 38}, {7, 38}, {8, 38}, {9, 38}, {10, 38}, {11, 38}, {12, 38}, {13, 38},
	            {14, 38}, {15, 38}, {16, 38}, {17, 38}, {18, 38}, {19, 38}, {20, 38}, {21, 38},
	            {22, 38}, {23, 38}, {24, 38}, {25, 38}, {26, 38}, {27, 38}, {28, 38}, {29, 38},
	            {30, 38}, {31, 38}, {32, 38}, {33, 38}, {34, 38}, {35, 38}, {36, 38}, {37, 38},
	            {38, 38}, {39, 38}, {40, 38}, {41, 38}, {42, 38}, {43, 38}, {44, 38}, {45, 38},
	            {46, 38}, {47, 38}, {48, 38}, {49, 38}, {50, 38}, {51, 38}, {52, 38}, {53, 38},
	            {54, 38}, {55, 38}, {56, 38}, {57, 38}, {58, 38}, {59, 38}
	        };
		
		int[][] blueCoordinates = {{4, 18}, {5, 18}, {6, 18}, {7, 18}, {8, 18}, {9, 18}, {10, 18}, {11, 18}, {12, 18}, 
				{13, 18}, {14, 18}, {15, 18}, {16, 18}, {17, 18}, {18, 18}, {19, 18}, {20, 18}, {21, 18}, {22, 18}, 
				{23, 18}, {24, 18}, {25, 18}, {26, 18}, {27, 18}, {28, 18}, {29, 18}, {30, 18}, {31, 18}, {32, 18}, 
				{33, 18}, {34, 18}, {35, 18}, {36, 18}, {37, 18}, {38, 18}, {39, 18}, {40, 18}, {41, 18}, {42, 18}, 
				{43, 18}, {44, 18}, {45, 18}, {46, 18}, {47, 18}, {48, 18}, {49, 18}, {50, 18}, {51, 18}, {52, 18}, 
				{53, 18}, {54, 18}, {55, 18}, {56, 18}, {57, 18}, {58, 18}, {59, 18}, {60, 18}, {3, 22}, {4, 22}, 
				{5, 22}, {6, 22}, {7, 22}, {8, 22}, {9, 22}, {12, 22}, {13, 22}, {14, 22}, {15, 22}, {16, 22}, 
				{17, 22}, {20, 22}, {21, 22}, {22, 22}, {23, 22}, {24, 22}, {25, 22}, {26, 22}, {29, 22}, {30, 22}, 
				{31, 22}, {32, 22}, {33, 22}, {34, 22}, {35, 22}, {40, 22}, {41, 22}, {42, 22}, {48, 22}, {49, 22}, 
				{50, 22}, {51, 22}, {52, 22}, {53, 22}, {56, 22}, {61, 22}, {3, 23}, {9, 23}, {12, 23}, {23, 23}, 
				{29, 23}, {35, 23}, {39, 23}, {40, 23}, {42, 23}, {43, 23}, {53, 23}, {56, 23}, {61, 23}, {3, 24}, 
				{9, 24}, {12, 24}, {23, 24}, {29, 24}, {35, 24}, {38, 24}, {39, 24}, {43, 24}, {44, 24}, {53, 24}, 
				{56, 24}, {61, 24}, {3, 25}, {9, 25}, {12, 25}, {23, 25}, {29, 25}, {35, 25}, {38, 25}, {44, 25}, 
				{53, 25}, {56, 25}, {61, 25}, {3, 26}, {4, 26}, {5, 26}, {6, 26}, {7, 26}, {8, 26}, {9, 26}, {12, 26}, 
				{23, 26}, {29, 26}, {30, 26}, {31, 26}, {32, 26}, {33, 26}, {34, 26}, {35, 26}, {38, 26}, {44, 26}, 
				{53, 26}, {56, 26}, {61, 26}, {3, 27}, {4, 27}, {5, 27}, {12, 27}, {23, 27}, {29, 27}, {30, 27}, 
				{31, 27}, {38, 27}, {44, 27}, {53, 27}, {56, 27}, {57, 27}, {58, 27}, {59, 27}, {60, 27}, {61, 27}, 
				{3, 28}, {5, 28}, {6, 28}, {12, 28}, {13, 28}, {14, 28}, {15, 28}, {23, 28}, {29, 28}, {31, 28}, 
				{32, 28}, {38, 28}, {44, 28}, {48, 28}, {49, 28}, {50, 28}, {51, 28}, {52, 28}, {53, 28}, {61, 28}, 
				{3, 29}, {6, 29}, {7, 29}, {12, 29}, {23, 29}, {29, 29}, {32, 29}, {33, 29}, {38, 29}, {44, 29}, 
				{48, 29}, {61, 29}, {3, 30}, {7, 30}, {8, 30}, {12, 30}, {23, 30}, {29, 30}, {33, 30}, {34, 30}, 
				{38, 30}, {44, 30}, {48, 30}, {61, 30}, {3, 31}, {8, 31}, {9, 31}, {12, 31}, {23, 31}, {29, 31}, 
				{34, 31}, {35, 31}, {38, 31}, {39, 31}, {43, 31}, {44, 31}, {48, 31}, {61, 31}, {3, 32}, {9, 32}, 
				{12, 32}, {23, 32}, {29, 32}, {35, 32}, {39, 32}, {40, 32}, {42, 32}, {43, 32}, {48, 32}, {61, 32}, 
				{3, 33}, {9, 33}, {12, 33}, {13, 33}, {14, 33}, {15, 33}, {16, 33}, {17, 33}, {23, 33}, {29, 33}, {35, 33}, 
				{40, 33}, {41, 33}, {42, 33}, {48, 33}, {49, 33}, {50, 33}, {51, 33}, {52, 33}, {53, 33}, {61, 33}, {4, 37}, 
				{5, 37}, {6, 37}, {7, 37}, {8, 37}, {9, 37}, {10, 37}, {11, 37}, {12, 37}, {13, 37}, {14, 37}, {15, 37}, 
				{16, 37}, {17, 37}, {18, 37}, {19, 37}, {20, 37}, {21, 37}, {22, 37}, {23, 37}, {24, 37}, {25, 37}, {26, 37}, 
				{27, 37}, {28, 37}, {29, 37}, {30, 37}, {31, 37}, {32, 37}, {33, 37}, {34, 37}, {35, 37}, {36, 37}, {37, 37}, 
				{38, 37}, {39, 37}, {40, 37}, {41, 37}, {42, 37}, {43, 37}, {44, 37}, {45, 37}, {46, 37}, {47, 37}, {48, 37}, 
				{49, 37}, {50, 37}, {51, 37}, {52, 37}, {53, 37}, {54, 37}, {55, 37}, {56, 37}, {57, 37}, {58, 37}, {59, 37}, 
				{60, 37}};

		
		//Alles Schwarz:
		for (int i = GraphicChip.VIDMEM_START; i < GraphicChip.VIDMEM_END; i++) {
			graphicChip.writeToVideoMemory(i, (byte) 0x00);
		}
		for (int[] coord : yellowCoordinates) {
			graphicChip.setVidMemPixel(coord, true, true);
		}
		
		for (int[] coord : whiteCoordinates) {
			graphicChip.setVidMemPixel(coord, true, false);
		}
		
		for (int[] coord : blueCoordinates) {
			graphicChip.setVidMemPixel(coord, false, true);
		}
	}
}
