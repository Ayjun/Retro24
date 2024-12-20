package common.util.validate;

import java.io.File;

/**
 * Validiert einen Dateipfad auf Gültigkeit
 * @author Eric Schneider
 */
public class FilePathValidator {
    private final String pathInQuestion;
    private final String fileExtension;

    /**
     * Erstellt einen neuen FilePathValidator.
     * 
     * @param pathInQuestion zu prüfender Dateipfad
     * @param fileExtension erwartete Dateiendung (z.B. ".bin")
     */
    public FilePathValidator(String pathInQuestion, String fileExtension) {
    	if (pathInQuestion == null || pathInQuestion.isEmpty()) {
        	throw new IllegalArgumentException("Pfad darf nicht leer sein!");
        }
        if (fileExtension == null || fileExtension.isEmpty()) {
        	throw new IllegalArgumentException("Dateierweiterung darf nicht leer sein!!");
        }
        this.pathInQuestion = pathInQuestion;
        this.fileExtension = fileExtension.startsWith(".") ? fileExtension : "." + fileExtension;
    }

    /**
     * Prüft ob der Pfad auf eine existierende Datei mit der richtigen Endung zeigt.
     * 
     * @return true wenn der Pfad gültig ist, sonst false
     */
    public boolean validate() {    
        File file = new File(pathInQuestion);
        return file.isFile() && file.getName().endsWith(fileExtension);
    }
    
    public String getPath() {
    	return pathInQuestion;
    }
    public String getFileExtension() {
    	return fileExtension;
    }
}