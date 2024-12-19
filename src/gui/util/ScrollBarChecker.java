package gui.util;

import javafx.scene.Node;

public class ScrollBarChecker {
	
	/**
	 * Prüft ob ein Objekt eine javaFX Scrollbar ist.
	 * @param target das zu untersuchende Objekt
	 * @return true wenn Scrollbar, sonst false
	 */
	public static boolean isScrollBar(Object target) {
	    if (target instanceof javafx.scene.control.ScrollBar) {
	        return true;
	    }
	    if (target instanceof Node) {
	        Node node = (Node) target;
	        while (node != null) {
	            if (node instanceof javafx.scene.control.ScrollBar) {
	                return true;
	            }
	            node = node.getParent();
	        }
	    }
	    return false;
	}
}
