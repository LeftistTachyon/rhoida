package com.github.leftisttachyon.input;

/**
 * An {@link Exception} that represents a incorrectly formatted TAS replay file.
 */
public class InvalidFileFormatException extends RuntimeException {
    /**
     * Creates a new InvalidFileException
     */
    public InvalidFileFormatException() {
        super();
    }

    /**
     * Creates a new InvalidFileException
     *
     * @param message the message
     */
    public InvalidFileFormatException(String message) {
        super(message);
    }
}
