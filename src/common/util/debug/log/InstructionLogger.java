package common.util.debug.log;

import java.util.Collection;

/**
 * Logger für Instruction Dumps mit Instruktionszähler.
 * @author Eric Schneider
 */
public class InstructionLogger extends Logger {

    public InstructionLogger(Dumper dumper) {
        super(dumper);
    }
    
    /**
     * Erstellt einen neuen Log-Eintrag mit dem aktuellen Instruction Dump 
     * und Instruktionszähler.
     */
    public void log() {
        String dump = dumper.dump();
        if (dump == null) {
            return;
        }
        
        incrementInstructionCounter();
        
        StringBuilder logEntry = new StringBuilder()
            .append("### Instruction Number: ")
            .append(instructionCounter)
            .append(System.lineSeparator())
            .append(dump);
            
        log.offer(logEntry.toString());
    }
    
    /**
     * Überträgt die angesammelten Logs in die übergebene Collection.
     * 
     * @param collection Die Collection in die die Logs übertragen werden sollen
     */
    public void drainTo(Collection<String> collection) {
        log.drainTo(collection);
    }

    /**
     * Erhöht den Instruktionszähler um 1.
     */
    public void incrementInstructionCounter() {
        instructionCounter++;
    }
}
