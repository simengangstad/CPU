package io.simengangstad.github.cpu;

/**
 * @author simengangstad
 * @since 29.11.14
 */
public abstract class AttachableHardware {

    /**
     * The memory of the CPU attached to.
     */
    protected Memory memory;

    /**
     * Processes an array of arguments. Mind that the array may be bigger
     * than the amount of arguments that shall be processed. Therefore
     * a size variable is given to clarify the amount. Reducing memory
     * footprint is the reason for this design decision.
     *
     * @param args An array where the arguments are located.
     * @param size The amount of arguments passed.
     *
     * @throws RuntimeException If some error occured during the processing.
     */
    public abstract void process(int[] args, int size) throws RuntimeException;
}
