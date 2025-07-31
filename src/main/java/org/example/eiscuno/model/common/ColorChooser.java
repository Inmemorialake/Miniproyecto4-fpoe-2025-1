package org.example.eiscuno.model.common;

/**
 * Functional interface for choosing a color.
 * This interface can be implemented to provide a method for selecting a color.
 */
@FunctionalInterface
public interface ColorChooser {
    String chooseColor();
}