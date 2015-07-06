package io.simengangstad.github.cpu;

import io.simengangstad.github.cpu.exception.MemoryException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Memory with arbitrary capasity.
 *
 * @author simengangstad
 * @since 09.08.14
 */
public class Memory {

    // TODO: HashMap might be a slow way to organise this..

    /**
     * The pool of addresses which refer to the bytes of memory.
     */
    private final HashMap<Integer, byte[]> ram = new HashMap<>();

    /**
     * The maximum amount of bytes.
     */
    public final int capasity;

    /**
     * Allocated memeory in bytes.
     */
    private int allocatedBytes = 0;

    /**
     * Used for generating pointers.
     */
    private Random random = new Random();

    /**
     * Initializes the memory with a capasity.
     */
    public Memory(int capasity) {

        this.capasity = capasity;
    }

    /**
     * Allocates memory.
     *
     * @param bytes The amount of bytes that shall be allocated.
     *
     * @return The address to the bytes that are being allocated.
     *
     * @throws MemoryException If the allocation request exceeds the capasity of the memory (out of memory).
     */
    public int alloc(int bytes) throws MemoryException {

        if (allocatedBytes + bytes > capasity) {

            throw new MemoryException("Out of memory. Tried allocting " + bytes + " bytes. Allocted memory before request: " + allocatedBytes + " bytes.");
        }

        int address = random.nextInt();

        // Loop until a unique address is found.
        while (ram.containsKey(address)) {

            // System.err.println("Identical address already exists, trying another one.");

            address = random.nextInt();
        }

        byte[] values = new byte[bytes];

        Arrays.fill(values, (byte) 0x0);

        ram.put(address, values);

        allocatedBytes += bytes;

        return address;
    }

    /**
     * Frees memory.
     *
     * @param address The address to the bytes that are requested being freed.
     *
     * @throws MemoryException If the address isn't valid.
     */
    public void free(int address) throws MemoryException {

        if (isValidAddress(address)) {

            allocatedBytes -= ram.get(address).length;

            ram.remove(address);
        }
    }

    /**
     * Sets bytes in memory.
     *
     * @param address The address of the bytes to set.
     * @param replacingBytes The replacing bytes.
     * @param offset The offset where the replacing bytes are placed. Used for only replacing
     *               certain parts of an allocated space.
     *
     * @throws MemoryException If the offset isn't signed, if the address isn't valid or if
     *                         the offset exceeds the bounds of the allocated space.
     */
    public void set(int address, byte[] replacingBytes, int offset) throws MemoryException {

        if (offset < 0) {

            throw new MemoryException("Offset has to be signed.");
        }

        if (isValidAddress(address)) {

            byte[] bytes = ram.get(address);

            if (offset + replacingBytes.length > bytes.length) {

                throw new MemoryException("Offset exceeds bounds of allocted space withing address.");
            }

            System.arraycopy(replacingBytes, 0, bytes, offset, replacingBytes.length);
        }
    }

    /**
     * Returns bytes from memory.
     *
     * @param address The address where the bytes are located.
     * @param offset The offset within the allocated space.
     * @param size The amount of bytes that are requested to be returned.
     *
     * @return The bytes at the specified address within the offset and size.
     *
     * @throws MemoryException If the offset isn't signed, if the address isn't valid or
     *                         if the offset and size exceeds the bounds of the allocated
     *                         space.
     */
    public byte[] get(int address, int offset, int size) throws MemoryException {

        if (offset < 0) {

            throw new MemoryException("Offset has to be signed.");
        }

        if (isValidAddress(address)) {

            byte[] bytes = ram.get(address);

            if (offset + size > bytes.length) {

                throw new MemoryException("Offset and size exceeds bounds of allocated memory within address.");
            }

            byte[] returningBytes = new byte[size];

            System.arraycopy(bytes, offset, returningBytes, 0, size);

            return returningBytes;
        }

        return null;
    }

    /**
     * Resets the memory.
     */
    public void reset() {

        allocatedBytes = 0;

        ram.clear();
    }

    /**
     * @throws MemoryException If the address isn't valid.
     *
     * @return If the specified address is valid.
     */
    public boolean isValidAddress(int address) throws MemoryException {

        if (!ram.containsKey(address)) {

            throw new MemoryException("Not a valid memory address '" + address + "'.");
        }

        return true;
    }

    /**
     * Dumps the values in memory.
     */
    public void dump() {

        System.out.println("Allocated " + allocatedBytes + "/" + capasity + " bytes of memory:");

        ram.forEach((address, bytes) -> {

            System.out.print(address + ": " + "[");

            for (int c = 0; c < bytes.length; c++) {

                System.out.print(toBinaryString(bytes[c]) + (c != (bytes.length - 1) ? ", " : "]"));
            }

            System.out.println();
        });
    }

    private String toBinaryString(byte value) {

        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(" ", "0");
    }
}
