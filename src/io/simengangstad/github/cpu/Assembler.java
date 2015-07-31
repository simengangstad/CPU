package io.simengangstad.github.cpu;

import io.simengangstad.github.cpu.exception.AssembleException;

import java.util.ArrayList;

/**
 * @author simengangstad
 * @since 05.08.14
 */
public class Assembler {

    /**
     * Enum for elements that specify specific behaviours with the compiled code.
     */
    public enum LanguageFeature {

        Comment     ("//"),
        Label       ("@");

        String identifier;

        LanguageFeature(String identifier) {

            this.identifier = identifier;
        }
    }

    /**
     * Class for labels which can be used for easier jumping to arbitrary places within the code.
     */
    public class Label {

        /**
         * The name of the label.
         */
        public final String identifier;

        /**
         * The instruction after the label this label is pointing to.
         */
        public final int index;

        /**
         * Initializes the label with an indentifier and an index.
         */
        public Label(String identifier, int index) {

            this.identifier = identifier;
            this.index = index;
        }
    }

    /**
     * An enum which stores information about the standard instructions with an identifier,
     * a  minimum amount of arguments variable and a maximum amount of arguments variable
     * (-1 declares that the instruction can have n number of arguments).
     */
    public enum InstructionInformation {

        SET         (0x00, 2, 2),
        GET         (0x01, 2, 2),

        ADD         (0x10, 2, 2),
        SUB         (0x11, 2, 2),
        MUL         (0x12, 2, 2),
        DIV         (0x13, 2, 2),
        MOD         (0x14, 2, 2),

        AND         (0x20, 2, 2),
        OR          (0x21, 2, 2),
        XOR         (0x22, 2, 2),
        NOT         (0x23, 1, 1),
        SHR         (0x24, 2, 2),
        SHL         (0x25, 2, 2),
        USHR        (0x26, 2, 2),

        IFE         (0x30, 2, 2),
        IFN         (0x31, 2, 2),
        IFG         (0x32, 2, 2),
        IFL         (0x33, 2, 2),

        JSR         (0x40, 1, 1),

        HDP         (0x50, 1, -1),
        HRT         (0x51, 1, -1);

        int identifier, minimumAmountOfArguments, maximumAmountOfArguments;

        InstructionInformation(int identifier, int minimumAmountOfArguments, int maximumAmountOfArguments) {

            this.identifier = identifier;
            this.minimumAmountOfArguments = minimumAmountOfArguments;
            this.maximumAmountOfArguments = maximumAmountOfArguments;
        }
    }

    /**
     * The registers.
     */
    public static final String[] Registers = {"A", "B", "C", "X", "Y", "Z", "I", "J", "PC", "SP", "PUSH|POP", "PEEK", "EX"};

    /**
     * Assembles the input.
     */
    public int[] assemble(String input, boolean debug) throws AssembleException {

        String[] lines = input.split("\n");

        if (debug) System.out.println("Assembling " + lines.length + " lines of code.");

        int[] instructionList = new int[0];
        int counter = 0;

        ArrayList<Label> labels = new ArrayList<>();
        int instructionCounter = 0;

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i].trim();

            if (line.startsWith(LanguageFeature.Label.identifier)) {

                labels.add(new Label(line.substring(1).replace(" ", ""), instructionCounter));

                continue;
            }

            if (line.isEmpty() || line.startsWith(LanguageFeature.Comment.identifier)) {

                continue;
            }

            instructionCounter++;
        }

        boolean blockComment = false;
        boolean settingBlockComment = false;

        for (int i = 0; i < lines.length; i++) {

            settingBlockComment = false;

            String unformattedLine = lines[i].trim();
            String line = lines[i].trim();

            if (debug) System.out.println((i + 1) + ". " + line);

            int commentIndex = line.indexOf("//");
            int blockCommentIndex = line.indexOf("/*");
            int blockCommentEnd = line.indexOf("*/");

            if (commentIndex != -1 && !blockComment) {

                line = unformattedLine.substring(0, commentIndex);
            }
            else if (blockCommentIndex != -1 && !blockComment) {

                blockComment = true;

                settingBlockComment = true;

                line = unformattedLine.substring(0, blockCommentIndex);
            }

            if (blockCommentEnd != -1 && blockComment) {

                if (settingBlockComment) {

                    line = unformattedLine.replace(unformattedLine.substring(blockCommentIndex, blockCommentEnd + 2), "");
                }
                else {

                    blockComment = false;

                    line = unformattedLine.substring(blockCommentEnd + 2, unformattedLine.length());
                }
            }

            if (blockComment && !settingBlockComment) {

                continue;
            }

            line = line.trim();

            if (line.isEmpty() || line.startsWith(LanguageFeature.Label.identifier)) {

                continue;
            }

            String[] components = line.split(" ");

            int[] instruction = new int[1 + 1 + (components.length - 1) * 3];

            instruction[0] = instruction.length;

            InstructionInformation instructionInformationInfo = getInstructionFromName(components[0]);

            if (instructionInformationInfo == null) {

                throw new AssembleException(i + 1, "Error assembling input: '" + components[0] + "'. Not a valid instruction.");
            }
            else if (((instruction.length - 2) / 3 < instructionInformationInfo.minimumAmountOfArguments || ((instruction.length - 2) / 3 > instructionInformationInfo.maximumAmountOfArguments) && instructionInformationInfo.maximumAmountOfArguments != -1)) {

                throw new AssembleException(i + 1, "Error assembling input: '" + line + "'. Not a valid amount of arguments.");
            }
            else {

                instruction[1] = instructionInformationInfo.identifier;
            }

            for (int c = 2; c < instruction.length; c += 3) {

                boolean registerValue = false, retrieveFromMemory = false;

                int value = -1;

                String component = components[1 + (c - 2) / 3];

                try {

                    if (component.startsWith("*")) {

                        retrieveFromMemory = true;

                        component = component.substring(1);
                    }

                    value = Integer.parseUnsignedInt(component);
                }
                catch (NumberFormatException numberFormatException) {

                    String testValue = component.toUpperCase();

                    try {

                        // Try hexadecimal or binary.

                        if (testValue.startsWith("0X")) {

                            value = Integer.parseUnsignedInt(testValue.substring(2), 16);
                        }
                        else if (testValue.startsWith("0B")) {

                            value = Integer.parseUnsignedInt(testValue.substring(2), 2);
                        }
                        else {

                            if (component.equalsIgnoreCase("POP") && c == 2) {

                                throw new AssembleException(i + 1, "Can't set POP, use PUSH or PEEK instead.");
                            }

                            if (component.equalsIgnoreCase("PUSH") && c == 2 + 3) {

                                throw new AssembleException(i + 1, "Can't retrieve from PUSH, use POP or PEEK instead.");
                            }

                            if (component.equalsIgnoreCase("POP") || component.equalsIgnoreCase("PUSH")) {

                                retrieveFromMemory = true;
                                registerValue = true;

                                value = 0x0A;
                            }

                            for (String register : Registers) {

                                if (component.equalsIgnoreCase(register)) {

                                    if (register.equalsIgnoreCase("PEEK")) {

                                        retrieveFromMemory = true;
                                    }

                                    registerValue = true;

                                    value = getRegisterFromName(register);
                                }
                            }

                            for (Label label : labels) {

                                if (component.equalsIgnoreCase(label.identifier)) {

                                    value = label.index;
                                }
                            }

                            if (value == -1) {

                                // Value is not a qualified integer or register
                                throw new AssembleException(i + 1, "Error assembling input: '" + components[1 + (c - 2) / 3] + "'.");
                            }
                        }
                    }
                    catch (NumberFormatException numberFormatException1) {

                        throw new AssembleException(i + 1, "Error assembling input: '" + components[1 + (c - 2) / 3] + "' (might exceed bounds of 32 bit unsigned integer).");
                    }
                }

                instruction[c] = value;
                instruction[c + 1] = !retrieveFromMemory ? 0 : 1;
                instruction[c + 2] = !registerValue ? 0 : 1;
            }

            int[] oldInstructionList = instructionList;
            instructionList = new int[counter + instruction.length];
            System.arraycopy(oldInstructionList, 0, instructionList, 0, counter);

            System.arraycopy(instruction, 0, instructionList, counter, instruction.length);

            counter += instruction.length;

        }

        return instructionList;
    }

    /**
     * @return The instruction values based on a name.
     */

    public static InstructionInformation getInstructionFromName(String name) {

        for (InstructionInformation instructionInformation : InstructionInformation.values()) {

            if (instructionInformation.name().equalsIgnoreCase(name)) {

                return instructionInformation;
            }
        }

        return null;
    }

    /**
     * @return The register value based on a name.
     */

    public static int getRegisterFromName(String name) {

        if (name.equalsIgnoreCase("PUSH") || name.equalsIgnoreCase("POP")) {

            return 0x0A;
        }

        for (int i = 0; i < Registers.length; i++) {

            if (Registers[i].equalsIgnoreCase(name)) {

                return i;
            }
        }

        return -0x1;
    }
}
