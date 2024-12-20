package gui.controller.validator;

import static common.util.StringUtil.hexStringToInt;

import core.Retro24;
import gui.controller.ControlPanelController;
import common.util.validate.FilePathValidator;
import common.util.validate.MemoryDumpValidator;

/**
 * ControlPanelValidator ist zuständig für die Kontrolle / Validierung
 * von Inputs aus der ControlPanelView via dem ControlPanelController.
 * @author Eric Schneider
 */
public class ControlPanelValidator {
	// Referenz auf den controller:
	private final ControlPanelController controlPanelController;
	// Validator für Speicheraddressen:
	private MemoryDumpValidator memDumpValidator;
	// Validator für Programmpfade:
	private FilePathValidator filePathValidator;
	
	/**
	 * Instanziieren eines neuen ControlPanelValidator
	 * @param controlPanelController Referenz auf den controller
	 */
	public ControlPanelValidator(ControlPanelController controlPanelController) {
		this.controlPanelController = controlPanelController;
	}
	
	/**
	 * Prüft die Addressen für den Memory Dump auf Gültigkeit.
	 * @return true wenn gültig, sonst false
	 */
	public boolean validateMemoryDump() {
		try {
			this.memDumpValidator = new MemoryDumpValidator(
					Retro24.MEMORY_START, 
					Retro24.MEMORY_END, 
					ControlPanelController.MAXMEMDUMP);
			
			int startAddress = hexStringToInt(controlPanelController.memoryDumpInputFromSP().get());
			int endAddress = hexStringToInt(controlPanelController.memoryDumpInputToSP().get());
			
			if (!memDumpValidator.validateAddress(startAddress)) {
				controlPanelController.showError("Ungültige Startaddresse!",
						"Die Startaddresse liegt außerhalb des gültigen Bereichs.");
				return false;
			}
			
			if (!memDumpValidator.validateAddress(endAddress)) {
				controlPanelController.showError("Ungültige Endaddresse!",
						"Die Endaddresse liegt außerhalb des gültigen Bereichs.");
				return false;
			}
			
			if (!memDumpValidator.validateAddressRange(startAddress, endAddress)) {
				controlPanelController.showError("Ungültiger Addressbereich!",
						"Der gewählte Addressbereich ist zu groß oder ungültig." + 
						System.lineSeparator() +
						"Der Addresbereich darf maximal " + 
						String.format("0x%04X", memDumpValidator.getMaxRange())  + 
						" groß sein.");
				return false;
			}
		}
		catch (IllegalArgumentException e) {
			controlPanelController.showError("Ungültige Addresse!",
					"Bitte Zahlenwerte in hexadezimal Schreibweise (0x) verwenden." + 
			System.lineSeparator() +
			"Bsp.: 0x1ABC");
			return false;
		}

		return true;
	}
	
	/**
	 * Prüft den im Input Feld angegebenen Programmpfad auf Gültigkeit.
	 * @return true wenn gültig, sonst false
	 */
	public boolean validateProgramPath() {
		try {
			this.filePathValidator = new FilePathValidator(
					controlPanelController.pathInputTextSP().get(), 
					Retro24.SUPPORTED_FILE_EXTENSION);
			
		} catch (IllegalArgumentException e) {
			controlPanelController.showError("Ungültige Datei!", e.getMessage());
			return false;
		}
		
		if (!filePathValidator.validate()) {
			controlPanelController.showError("Ungültige Datei!",
					"Das angegebene Programm existiert nicht oder wird nicht unterstützt.");
			return false;
		}
		
		return true;
	}
}
