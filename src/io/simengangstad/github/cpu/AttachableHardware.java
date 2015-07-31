package io.simengangstad.github.cpu;

/**
 * @author simengangstad
 * @since 29.11.14
 */
public abstract class AttachableHardware {

    /**
     * The memory of the CPU the hardware is attached to.
     */
    protected Memory memory;

    /**
     * Processes an array of arguments. Mind that the array may be bigger
     * than the amount of arguments that shall be processed. Therefore
     * a size variable is given to clarify the amount. Reducing memory
     * footprint is the reason for this design decision.
     *
     * @param arguments An array where the arguments are located.
     * @param size The amount of arguments passed.
     *
     * @throws RuntimeException If some error occured during the processing.
     */
    public abstract void process(int[] arguments, int size) throws RuntimeException;

    /**
     * Returns data from the hardware. Mind that the array holding the arguments may be bigger
     * than the amount of arguments that specify the data to be returned.
     * Therefore a size variable is given to clarify the amount. Reducing
     * memory footprint is the reason for this design decision.
     *
     * @param arguments The arguments that specifies which data that is to be returned.
     * @param size The amount of arguments passed.
     *
     * @return The data reguested by the arguments.
     *
     * @throws RuntimeException If some error occured during the retrieving.
     */
    public abstract int retrieve(int[] arguments, int size) throws RuntimeException;
}
