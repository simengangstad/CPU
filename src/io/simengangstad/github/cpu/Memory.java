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
     * Resets the memory.
     */
    public void reset() {

        Arrays.fill(ram, 0);
    }

    /**
     * Dumps the values of the memory.
     */
    public void dump() {

        for (int i = 0; i < ram.length; i++) {

            System.out.println("0x" + Integer.toHexString(i) + ": " + "0b" + Integer.toBinaryString(ram[i]));
        }
    }
}
