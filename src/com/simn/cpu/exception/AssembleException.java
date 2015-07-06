package com.simn.cpu.exception;

/**
 * @author simengangstad
 * @since 28.06.15
 */
public class AssembleException extends Exception {

    /**
     * Constructs an assemble exception without a specific message.
     */
    public AssembleException() {

        super();
    }

    /**
     * Constructs an assemble exception with a line number.
     *
     * @param line The line number where the exception occurred.
     */
    public AssembleException(int line) {

        super("Line " + line);
    }

    /**
     * Constructs an assemble exception with a line number and a message.
     *
     * @param line The line where the exception occurred.
     * @param message The message of the exception.
     */
    public AssembleException(int line, String message) {

        super("Line " + line + ": " + message);
    }
}
