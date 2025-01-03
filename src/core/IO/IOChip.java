package core.IO;

import core.Retro24;

public class IOChip {
	public final static int JOYSTICK_PORT = 0x0020;
	public final static byte JOYSTICK_UP = 0b00000001;
	public final static byte JOYSTICK_DOWN = 0b00000010;
	public final static byte JOYSTICK_LEFT = 0b00000100;
	public final static byte JOYSTICK_RIGHT = 0b00001000;
	public final static byte JOYSTICK_FIRE = 0b00010000;
	
	private final Retro24 retro24;
	
	// Bit 0 ist 1 wenn „oben“, 
	// Bit 1 ist 1 wenn „unten“, 
	// Bit 2 ist 1 wenn „links“, 
	// Bit 3 ist 1 wenn „rechts“, 
	// Bit 4 ist 1 wenn „Feuer“
	
	public IOChip(Retro24 retro24) {
		this.retro24 = retro24;
	}
	
	public void writeJoystickMovement(byte movementByte) {
		retro24.writeMemory(JOYSTICK_PORT, movementByte);
	}
}
