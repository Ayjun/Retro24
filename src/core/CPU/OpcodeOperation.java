package core.CPU;

/** 
 * Funktionales Interface für Opcodes / deren Funktion
 */
public interface OpcodeOperation {
	byte[] execute(CPU cpu);
}
