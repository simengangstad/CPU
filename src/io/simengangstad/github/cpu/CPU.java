package io.simengangstad.github.cpu;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author simengangstad
 * @since 05.08.14
 */
public class CPU {

    /**
     * Instructions.
     */
    public static final int

            SET             = 0x00,
            GET             = 0x01,

            ADD             = 0x10,
            SUB             = 0x11,
            MUL             = 0x12,
            DIV             = 0x13,
            MOD             = 0x14,

            AND             = 0x20,
            OR              = 0x21,
            XOR             = 0x22,
            NOT             = 0x23,
            SHR             = 0x24,
            SHL             = 0x25,
            USHR            = 0x26,

            IFE             = 0x30,
            IFN             = 0x31,
            IFG             = 0x32,
            IFL             = 0x33,

            HDISPATCH       = 0x50;

    /**
     * Return values from get instruction.
     */
    private static final int

            MEMORY_CAPASITY = 0x0,
            TIME_SINCE_BOOT = 0x1;

    /**
     * Program counter, stack pointer and extra.
     */
    private static final int PC = 0x8,
                             SP = 0x9,
                             EX = 0xA;

    /**
     * Instruction counter. Used for tracking within instructions.
     */
    private int instructionCounter = 0;

    /**
     * Holds values to translate program count values to instruction count values.
     */
    private int[] instructionCounterArray;

    /**
     * The 32 bit values which can hold data (registers, pc, sp and ex).
     */
    private int[] values = new int[11];

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
     * The attached hardware of the CPU.
     */
    private HashMap<Integer, AttachableHardware> attachableHardware = new HashMap<>();

    /**
     * Variables used to reduce memory footprint.
     */
    private int[] hardwareDispatchArgs = new int[2];
    private int[] arguments = new int[2];

    /**
     * Constructs the CPU.
     */
    public CPU() {

        memory = new Memory(512);
    }

    /**
     * Attaches hardware.
     *
     * @param location The location to attach the hardware.
     * @param attachableHardware The hardware that is being attached to the CPU.
     */
    public void attachHardware(int location, AttachableHardware attachableHardware) {

        if (this.attachableHardware.containsKey(location)) {

            fault("Already attached hardware at locaiton " + location + ". Select a different location.");
        }

        attachableHardware.memory = memory;

        this.attachableHardware.put(location, attachableHardware);
    }

    /**
     * Detaches hardware.
     *
     * @param location The location of the hardware.
     */
    public void detachHardware(int location) {

        attachableHardware.get(location).memory = null;

        attachableHardware.remove(location);
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

        int offset;

        long timeBeforeInstruction;

        while (values[PC] + instructionCounter < program.length) {

            timeBeforeInstruction = System.currentTimeMillis();

            offset = program[values[PC] + instructionCounter];

            if (passCycle == 0) {

                executeInstruction(program);
            }

            passCycle -= (passCycle > 0 ? 1 : 0);

            if (!settingProgramCounter) {

                values[PC]++;
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
     * Executes the instruction.
     */
    private void executeInstruction(int program[]) {

        try {

            int amountOfArguments = (program[values[PC] + instructionCounter] - 2) / 3;
            int address = -1;
            boolean writeToMemory = false;

            if (arguments.length < amountOfArguments) {

                arguments = new int[amountOfArguments];
            }

            for (int i = 0; i < amountOfArguments; i++) {

                int value                  = program[values[PC] + instructionCounter + 2 + 3 * i];
                boolean retrieveFromMemory = program[values[PC] + instructionCounter + 3 + 3 * i] == 1;
                boolean register           = program[values[PC] + instructionCounter + 4 + 3 * i] == 1;

                if (retrieveFromMemory) {

                    if (i == 0) {

                        writeToMemory = true;

                        address = register ? values[value] : value;
                    }

                    arguments[i] = register ? memory.get(values[value]) : memory.get(value);
                }
                else {

                    if (i == 0) {

                        if (!register && amountOfArguments >= 2) {

                            fault("Invalid argument: " + "'" + value + "'.");
                        }

                        address = value;
                    }

                    arguments[i] = register ? values[value] : value;
                }
            }

            switch (program[values[PC] + instructionCounter + 1]) {

                case SET:

                    arguments[0] = arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case GET:

                    switch (arguments[1]) {

                        case MEMORY_CAPASITY:

                            setValue(address, writeToMemory, memory.capasity());

                            break;

                        case TIME_SINCE_BOOT:

                            setValue(address, writeToMemory, time);

                            break;
                    }

                    break;

                case ADD:

                    if (arguments[0] + arguments[1] > 0xffffffff) {

                        values[EX] = 0x0001;
                    }

                    arguments[0] += arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case SUB:

                    if (arguments[0] - arguments[1] < 0x0) {

                        values[EX] = 0xffff;
                    }

                    arguments[0] -= arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case MUL:

                    arguments[0] *= arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case DIV:

                    if (arguments[1] == 0) {

                        values[EX] = 0x0;
                        arguments[0] = 0x0;
                    }
                    else {

                        arguments[0] /= arguments[1];
                    }

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case MOD:

                    if (arguments[1] == 0) {

                        values[EX] = 0x0;
                        arguments[0] = 0x0;
                    }
                    else {

                        arguments[0] %= arguments[1];
                    }

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case AND:

                    arguments[0] &= arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case OR:

                    arguments[0] |= arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case XOR:

                    arguments[0] ^= arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case NOT:

                    setValue(address, writeToMemory, ~arguments[0]);

                    break;

                case SHR:

                    arguments[0] = arguments[0] >> arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case SHL:

                    arguments[0] = arguments[0] << arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case USHR:

                    arguments[0] = arguments[0] >>> arguments[1];

                    setValue(address, writeToMemory, arguments[0]);

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

                case HDISPATCH:

                    if (arguments.length - 1 > hardwareDispatchArgs.length) {

                        hardwareDispatchArgs = new int[arguments.length - 1];
                    }

                    System.arraycopy(arguments, 1, hardwareDispatchArgs, 0, arguments.length - 1);

                    int location = arguments[0];

                    if (writeToMemory) {

                        location = memory.get(address);
                    }

                    attachableHardware.get(location).process(hardwareDispatchArgs, arguments.length - 1);

                    break;

                default:

                    fault("Unknown instruction or too many/few arguments in instruction.");
            }
        }
        catch (Exception exception) {

            exception.printStackTrace();

            fault(exception.getMessage());
        }
    }

    /**
     * Sets the value at the specified address.
     */
    private void setValue(int address, boolean writeToMemory, int value) {

        if (address == PC) {

            instructionCounter = instructionCounterArray[value] - (value);

            settingProgramCounter = true;
        }

        if (writeToMemory) {

            memory.set(address, value);
        }
        else {

            values[address] = value;
        }
    }

    /**
     * Warns that the current instruction isn't a valid instruction.
     */
    private void fault(String msg) {

        Exit.exit("Instruction fault at " + values[PC] + ": " + msg);
    }

    /**
     * Dumps the values.
     */
    public void dumpValues() {

        System.out.println(

                "Register A [" + values[0] + "]" + "\n" +
                "Register B [" + values[1] + "]" + "\n" +
                "Register C [" + values[2] + "]" + "\n" +
                "Register X [" + values[3] + "]" + "\n" +
                "Register Y [" + values[4] + "]" + "\n" +
                "Register Z [" + values[5] + "]" + "\n" +
                "Register I [" + values[6] + "]" + "\n" +
                "Register J [" + values[7] + "]" + "\n" +
                "PC         [" + values[PC] + "]" + "\n" +
                "SP         [" + values[SP] + "]" + "\n" +
                "EX         [" + values[EX] + "]" + "\n"
        );
    }

    /**
     * Resets the values.
     */
    private void reset() {

        for (int i = 0; i < values.length; i++) {

            values[i] = 0;
        }
    }
}
