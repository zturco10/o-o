package org.e2e.labe2e04.coordinate.exception;

import org.e2e.labe2e04.exception.ResourceNotFoundException;

public class CoordinateNotFoundException extends ResourceNotFoundException {
    public CoordinateNotFoundException() {
        super("Coordinate not found");
    }
}
