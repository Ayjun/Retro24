package common.util.debug.log;

import common.config.Config;
import core.Retro24;

public abstract class Dumper {
	protected final Retro24 retro24;
	protected final Config config;
    
    public Dumper(Retro24 retro24, Config config) {
    	this.retro24 = retro24;
        this.config = config;
    }
    
    /**
     * Erstellt einen formatierten Instruction Dump des aktuellen Prozessorzustands.
     * 
     * @return Formatierter Instruction Dump als String oder null wenn Dumps deaktiviert sind
     */
    public abstract String dump();
}
