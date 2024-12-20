package gui.util.debug.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import common.util.debug.log.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

/**
 * Observable Wrapper für Logger aus common.util.debug.log.
 * Diese werden hierüber um das observable BooleanProperty changedBP erweitert,
 * worüber von javaFX festgestellt werden kann, ob ein neues Element hinzugefügt wurde.
 * Es wird weitherhin sichergestellt, dass die Collection auf die gedraint wird,
 * maximal MAX_LOG_ENTRIES Elemente enthält.
 * @param <T>
 */
public class ObservableLogger <T extends Logger> {
	public static final int MAX_LOG_ENTRIES = 10000;
	
	private final T logger;
	private BooleanProperty changedBP;
	
	public ObservableLogger(T logger) {
		this.logger = logger;
		this.changedBP  = new SimpleBooleanProperty(false);
	}
	
	/**
     * Erstellt einen neuen Log-Eintrag mit dem aktuellen Instruction Dump 
     * und Instruktionszähler.
     */
    public void log() {
    	logger.log();
    	changedBP.set(true);
    }
    
    /**
     * Überträgt die angesammelten Logs in die übergebene Collection.
     *
     * @param collection Die Collection in die die Logs übertragen werden sollen
     */
    public void transferLogTo(Collection<String> collection) {
        // Temporäre Liste für die Logs
        List<String> tempLogs = new ArrayList<>();
        
        // Logs in temporäre Liste übertragen
        logger.drainTo(tempLogs);
        
        if (collection instanceof ObservableList<?>) {
            ObservableList<String> observableList = (ObservableList<String>) collection;
            
            Platform.runLater(() -> {
                // Gesamtgröße nach dem Hinzufügen berechnen
                int totalSize = observableList.size() + tempLogs.size();
                
                // Wenn die Gesamtgröße MAX_LOG_ENTRIES überschreitet
                if (MAX_LOG_ENTRIES < totalSize) {
                    // Zu viele Elemente am Anfang entfernen
                    int elementsToRemove = totalSize - MAX_LOG_ENTRIES;
                    if (elementsToRemove > observableList.size()) {
                        // Wenn mehr Elemente entfernt werden müssen, als in der Liste sind
                        observableList.clear();
                        // Nur die letzten MAX_LOG_ENTRIES Elemente aus tempLogs nehmen
                        int startIndex = Math.max(0, tempLogs.size() - MAX_LOG_ENTRIES);
                        observableList.addAll(tempLogs.subList(startIndex, tempLogs.size()));
                    } else {
                        // Elemente am Anfang der Liste entfernen
                        observableList.remove(0, elementsToRemove-1);
                        // Neue Logs hinzufügen
                        observableList.addAll(tempLogs);
                    }
                } else {
                    // Wenn Platz ist, einfach hinzufügen
                    observableList.addAll(tempLogs);
                }
            });
        } else {
            // Für nicht-ObservableList einfach hinzufügen
            collection.addAll(tempLogs);
        }
        
        changedBP.set(false);
    }
    
    public BooleanProperty changedBP() {
    	return this.changedBP;
    }
}
