package common.util.debug.log;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Log das Lognachrichten (jedoch maximal MAX_LOG_ENTRIES) enthält.
 */
public class Log {
    private static final int MAX_LOG_ENTRIES = 2500;

    private final BlockingDeque<String> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    
    public void drainTo(Collection<String> collection) {
        log.drainTo(collection);
    }

    public void offer(String record) {
        // Falls die Deque voll ist, wird das älteste Element entfernt
        if (!log.offer(record)) {
            log.pollFirst();  // Entferne ältestes Element
            log.offer(record); // Versuche erneut, das neue Element hinzuzufügen
        }
    }
    
    public String peekLast() {
    	return this.log.peekLast();
    }
    
    @Override
    public String toString() {
    	return String.join(System.lineSeparator(), log);	
    }
}