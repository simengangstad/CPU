package io.simengangstad.github.cpu;

import java.util.Arrays;

/**
 * Memory with arbitrary capasity.
 *
 * @author simengangstad
 * @since 09.08.14
 */
public class Memory {

    /**
     * The memory.
     */
    private final int[] ram;

    /**
     * Initializes the memory with a capasity.
     */
    public Memory(int words) {

        ram = new int[words];
    }

    /**
     * @return The capasity in words of the memory.
     */
    public int capasity() {

        return ram.length;
    }

    /**
     * @param address The address to the value.
     *
     * @return The value of the address.
     *
     * @throws RuntimeException If the address is invalid.
     */
    public int get(int address) throws RuntimeException {

        evaluateAddress(address);

        return ram[address];
    }

    /**
     * Sets the value at the given address.
     *
     * @throws RuntimeException If the address is invalid.
     */
    public void set(int address, int value) throws RuntimeException {

        evaluateAddress(address);

        ram[address] = value;
    }

    /**
     * Checks if the address given is valid.
     *
     * @throws RuntimeException If the address is invalid.
     */
    private void evaluateAddress(int address) throws RuntimeException {

        if (address < 0 || address >= ram.length) {

            throw new RuntimeException("Invalid memory address: " + "'" + address + "'.");
        }
    }

    /**
     * Copies a chunk of the memory into an array.
     *
     * @param sourcePosition The position in memory where the copy shall begin.
     * @param destination The destination array.
     * @param destinationPosition The position in the destination array where the copy shall be placed.
     * @param length The amount of words that shall be copied.
     */
    public void copyInto(int sourcePosition, int[] destination, int destinationPosition, int length) {

        System.arraycopy(ram, sourcePosition, destination, destinationPosition, length);
    }

    /**
     * Resets the memory.
     */
    public void reset() {

        Arrays.fill(ram, 0);
    }

    /**
     * Dumps the values of the memory in the specified area defined by a index and length.
     *
     * @param index The start index of the dump.
     * @param length The amount of elements getting dumped.
     */
    public void dump(int index, int length) {

        if (index < 0 || index + length >= ram.length) {

            throw new RuntimeException("Index and length out of bounds for memory.");
        }

        for (int i = index; i < length; i++) {

            System.out.println("0x" + Integer.toHexString(i) + ": 0x"  + Integer.toHexString(ram[i]));
        }
    }

    /**
     * Dumps the values of the memoory.
     */
    public void dump() {

        dump(0, ram.length - 1);
    }
}
