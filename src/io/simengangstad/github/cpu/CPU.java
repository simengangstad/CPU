package io.simengangstad.github.cpu;

import io.simengangstad.github.cpu.exception.MemoryException;

import java.util.ArrayList;

/**
 * @author simengangstad
 * @since 05.08.14
 */
public class CPU {

    /**
     * Instructions.
     */
    private static final int

            SET = 0x0,
            GET = 0x1,
            ADD = 0x2,
            SUB = 0x3,
            MUL = 0x4,
            DIV = 0x5,
            IFE = 0x6,
            IFN = 0x7,
            IFG = 0x8,
            IFL = 0x9,
            MALLOC = 0xA,
            MFREE = 0xB,
            MSET = 0xC,
            MGET = 0xD;

    /**
     * Return values from get instruction.
     */
    private static final int

            MEMORY_CAPASITY = 0x0,
            TIME_SINCE_BOOT = 0x1;

    /**
     * Program counter. Used for tracking instructions.
     */
    private static final int PC = 0x8;

    /**
     * Instruction counter. Used for tracking within instructions.
     */
    private int instructionCounter = 0;

    /**
     * Holds values to translate program count values to instruction count values.
     */
    private int[] instructionCounterArray;

    /**
     * The 32 bit register which can hold data.
     */
    private int[] registers = new int[9];

    /**
     * If the program should pass one instruction cycle.
     */
    private int passCycle = 0;

    /**
     * Determine that the current cycle is setting the program counter, and
     * should therefore not add the offset to the program counter after the
     * instruction.
     */
    private boolean settingProgramCounter = false;

    /**
     * The memory.
     */
    public Memory memory;

    /**
     * A variable which holds the value since boot.
     */
    private int time = 0x0;

    /**
     * Constructs the CPU.
     */
    public CPU() {

        memory = new Memory(0xFFFFFF);
    }

    /*
     * Executes the program.
     */
    public void execute(int[] program) {

        if (program == null) {

            return;
        }

        reset();

        // TODO: Might need optimization
        ArrayList<Integer> instructionCounterArrayBuild = new ArrayList<>();

        instructionCounterArrayBuild.add(0);

        for (int c = 0; c < program.length; c += program[c]) {

            instructionCounterArrayBuild.add(program[c] + (instructionCounterArrayBuild.size() > 0 ? instructionCounterArrayBuild.get(instructionCounterArrayBuild.size() - 1) : 0));
        }

        instructionCounterArray = new int[instructionCounterArrayBuild.size()];

        for (int c = 0; c < instructionCounterArray.length; c++) {

            instructionCounterArray[c] = instructionCounterArrayBuild.get(c);
        }

        int[] args = new int[0];
        int offset;

        int value;
        boolean isRegister;

        int index;

        long timeBeforeInstruction;

        while (registers[PC] + instructionCounter < program.length) {

            timeBeforeInstruction = System.currentTimeMillis();

            offset = program[registers[PC] + instructionCounter];

            if (passCycle == 0) {

                if ((offset - 1 - 1) / 2 > args.length) {

                    args = new int[(offset - 1 - 1) / 2];
                }

                for (index = 0; index < (offset - 1 - 1) / 2; index++) {

                    // Is the value receieven from a register?
                    isRegister = program[registers[PC] + instructionCounter + 1 + 1 + 1 + index * 2] == 1;

                    if (isRegister) {

                        // Value register is holding
                        value = registers[program[registers[PC] + instructionCounter + 1 + 1 + index * 2]];
                    }
                    else {

                        // Raw value

                        value = program[registers[PC] + instructionCounter + 1 + 1 + index * 2];
                    }

                    args[index] = value;
                }

                executeInternalInstruction(program, args);
            }

            passCycle -= (passCycle > 0 ? 1 : 0);

            if (!settingProgramCounter) {

                registers[PC]++;
                instructionCounter += (offset - 1);
            }

            settingProgramCounter = false;

            long delta = System.currentTimeMillis() - timeBeforeInstruction;

            if (time + delta > Integer.MAX_VALUE) {

                time -= Integer.MAX_VALUE;
            }

            time += delta;
        }
    }

    /**
     * Executes an internal instruction.
     *
     * @param program The whole program including all instructions.
     * @param arguments Arguments of the current instruction. This will include integers,
     *                  or if registers are given, the value of the register.
     */
    private void executeInternalInstruction(int[] program, int[] arguments) {

        int address = program[registers[PC] + instructionCounter + 2];

        try {

            switch (program[registers[PC] + instructionCounter + 1]) {

                case SET:

                    arguments[0] = arguments[1];

                    setValue(address, arguments[0]);

                    break;

                case GET:

                    switch (arguments[1]) {

                        case MEMORY_CAPASITY:

                            setValue(address, memory.capasity);

                            break;

                        case TIME_SINCE_BOOT:

                            setValue(address, time);

                            break;
                    }

                    break;

                case ADD:

                    arguments[0] += arguments[1];

                    setValue(address, arguments[0]);

                    break;

                case SUB:

                    arguments[0] -= arguments[1];

                    setValue(address, arguments[0]);

                    break;

                case MUL:

                    arguments[0] *= arguments[1];

                    setValue(address, arguments[0]);

                    break;

                case DIV:

                    if (arguments[1] == 0) {

                        fault("Can't divide by zero.");
                    }

                    arguments[0] /= arguments[1];

                    setValue(address, arguments[0]);

                    break;

                case IFE:

                    if (arguments[0] != arguments[1]) {

                        passCycle += 2;
                    }

                    break;

                case IFN:

                    if (arguments[0] == arguments[1]) {

                        passCycle += 2;
                    }

                    break;

                case IFG:

                    if (arguments[0] <= arguments[1]) {

                        passCycle += 2;
                    }

                    break;

                case IFL:

                    if (arguments[0] >= arguments[1]) {

                        passCycle += 2;
                    }

                    break;

                case MALLOC:

                    setValue(address, memory.alloc(arguments[1]));

                    break;

                case MFREE:

                    memory.free(arguments[0]);

                    break;

                case MSET: {

                    int value = arguments[1];

                    byte[] bytes = new byte[arguments[3]];

                    for (int i = 0; i < arguments[3]; i++) {

                        bytes[i] = (byte) (value >>> ((arguments[3] - 1) * 8 - i * 8));
                    }

                    memory.set(arguments[0], bytes, arguments[2]);

                    break;
                }
                case MGET: {

                    byte[] bytes = memory.get(arguments[1], arguments[2], arguments[3]);

                    int value = 0;

                    for (int i = 0; i < bytes.length; i++) {

                        // Sets the bits of the value at index of 32, 24, 16 and 8 equal to the bits in memory
                        // Do this by setting the first 8 bits equal to bytes[n] and pushing back eight bits per
                        // time this loop runs
                        value ^= ((bytes[i] & 0xFF) << ((bytes.length) * 8 - (i + 1) * 8));
                    }

                    // Makes the value only hold values <= 0xFFFFFFFF
                    value &= 0xFFFFFFFF;

                    setValue(address, value);

                    break;
                }
                default:

                    fault("Unknown instruction.");

                    break;
            }
        }
        catch (MemoryException memoryException) {

            fault(memoryException.getMessage());
        }
    }

    /**
     * Sets the value at the specified address.
     */
    private void setValue(int address, int value) {

        if (address == PC) {

            instructionCounter = instructionCounterArray[value] - (value);

            settingProgramCounter = true;
        }

        registers[address] = value;
    }

    /**
     * Warns that the current instruction isn't a valid instruction.
     */
    private void fault(String msg) {

        System.err.println("Instruction fault at: " + registers[PC] + ". " + msg);
    }

    /**
     * Dumps the registers.
     */
    public void dumpRegisters() {

        System.out.println(

                "CPU registers:" + "\n" +
                "Register A [" + registers[0] + "]" + "\n" +
                "Register B [" + registers[1] + "]" + "\n" +
                "Register C [" + registers[2] + "]" + "\n" +
                "Register X [" + registers[3] + "]" + "\n" +
                "Register Y [" + registers[4] + "]" + "\n" +
                "Register Z [" + registers[5] + "]" + "\n" +
                "Register I [" + registers[6] + "]" + "\n" +
                "Register J [" + registers[7] + "]" + "\n" +
                "PC [" + registers[PC] + "]" + "\n"
        );
    }

    /**
     * Resets the registers.
     */
    private void reset() {

        for (int i = 0; i < registers.length; i++) {

            registers[i] = 0;
        }
    }
}
