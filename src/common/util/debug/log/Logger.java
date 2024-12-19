package common.util.debug.log;

import java.util.Collection;

public abstract class Logger {
	protected final Log log;
	protected final Dumper dumper;
	protected int instructionCounter = 0;
    
    public Logger(Dumper dumper) {
        this.dumper = dumper;
        this.log = new Log();
    }
    
    /**
     * Erstellt einen neuen Log-Eintrag mit dem aktuellen Instruction Dump 
     * und Instruktionszähler.
     */
    public abstract void log();
    
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
