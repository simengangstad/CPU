package io.simengangstad.github.cpu.exception;

/**
 * @author simengangstad
 * @since 28.06.15
 */
public class MemoryException extends Exception {

    /**
     * Constructs a memory exception without a specific message.
     */
    public MemoryException() {

        super();
    }

    /**
     * Constructs a memory exception with a specific message.
     */
    public MemoryException(String message) {

        super(message);
    }
}
