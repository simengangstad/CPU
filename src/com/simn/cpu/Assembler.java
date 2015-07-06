package com.simn.cpu;

import com.simn.cpu.exception.AssembleException;

/**
 * @author simengangstad
 * @since 05.08.14
 */
public class Assembler {

    /**
     * The instructions.
     */
    private static final String[] Instructions = {

            "SET",
            "GET",
            "ADD",
            "SUB",
            "MUL",
            "DIV",
            "IFE",
            "IFN",
            "IFG",
            "IFL",
            "MALLOC",
            "MFREE",
            "MSET",
            "MGET",
    };

    /**
     * The registers.
     */
    public static final String[] Registers = {"A", "B", "C", "X", "Y", "Z", "I", "J", "PC"};

    /**
     * Assembles the input.
     */
    public int[] assemble(String input, boolean debug) throws AssembleException {

        String[] lines = input.split("\n");

        if (debug) System.out.println("Assembling " + lines.length + " lines of code.");

        int[] instrucitonList = new int[0];
        int counter = 0;

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i].trim();

            if (debug) System.out.println(i + ". " + line);

            if (line.startsWith("//") || line.equals("")) {

                continue;
            }

            String[] components = line.split(" ");

            int[] instruction = new int[1 + 1 + (components.length - 1) * 2];

            instruction[0] = 1 + 1 + (components.length - 1) * 2; // Length of instruction
            instruction[1] = getInstructionName(components[0]); // Instruction identifier

            for (int c = 2; c < instruction.length; c += 2) {

                boolean registerValue = false;

                int value = -1;

                try {

                    value = Integer.parseInt(components[1 + (c - 2) / 2]);
                }
                catch (NumberFormatException numberFormatException) {

                    if (components[1 + (c - 2) / 2].toUpperCase().startsWith("0X")) {

                        try {

                            // Try hexadecimal.
                            value = Integer.parseInt(components[1 + (c - 2) / 2].substring(2), 16);
                        }
                        catch (NumberFormatException numberFormatException1) {

                            throw new AssembleException(i + 1, "Error assembling input: '" + components[1 + (c - 2) / 2] + "' (might exceed bounds of 32 bit signed integer).");
                        }
                    }
                    else {

                        for (String register : Registers) {

                            if (components[1 + (c - 2) / 2].equalsIgnoreCase(register)) {

                                registerValue = true;

                                value = getRegisterFromName(register);
                            }
                        }

                        if (value == -1) {

                            // Value is not a qualified integer or register
                            throw new AssembleException(i + 1, "Error assembling input: '" + components[1 + (c - 2) / 2] + "'.");
                        }
                    }
                }

                instruction[c] = value;
                instruction[c + 1] = !registerValue ? 0 : 1;
            }

            int[] oldInstructionList = instrucitonList;
            instrucitonList = new int[counter + instruction.length];
            System.arraycopy(oldInstructionList, 0, instrucitonList, 0, counter);

            System.arraycopy(instruction, 0, instrucitonList, counter, instruction.length);

            counter += instruction.length;
        }

        return instrucitonList;
    }

    /**
     * @return The instruction values based on a name.
     */

    public static short getInstructionName(String name) {

        for (short i = 0; i < Instructions.length; i++) {

            if (Instructions[i].equalsIgnoreCase(name)) {

                return i;
            }
        }

        return -0x1;
    }

    /**
     * @return The register value based on a name.
     */

    public static short getRegisterFromName(String name) {

        for (short i = 0; i < Registers.length; i++) {

            if (Registers[i].equalsIgnoreCase(name)) {

                return i;
            }
        }

        return (short) -0x1;
    }
}
