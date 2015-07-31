package io.simengangstad.github.cpu;

import java.util.ArrayList;
import java.util.Arrays;
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

            JSR             = 0x40,

            HDP             = 0x50,
            HRT             = 0x51;

    /**
     * Return registers from get instruction.
     */
    private static final int

            MEMORY_CAPASITY = 0x0,
            TIME_SINCE_BOOT = 0x1;

    /**
     * Program counter, stack pointer and extra.
     */
    private static final int PC         = 0x8,
                             SP         = 0x9,
                             PUSH_POP   = 0xA,
                             PEEK       = 0xB,
                             EX         = 0xC;

    /**
     * Instruction counter. Used for tracking within instructions.
     */
    private int instructionCounter = 0;

    /**
     * Holds registers to translate program count registers to instruction count registers.
     */
    private int[] instructionCounterArray;

    /**
     * The unsigned 32 bit registers which can hold data (a, b, c, x, y, i, j, pc, sp and ex).
     */
    private int[] registers = new int[13];

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
     * A variable which holds the value since boot. Gets reset after 0xffffffff milliseconds, or let me do the math for you...
     * 49.71 days.
     */
    private int time = 0x0;

    /**
     * The attached hardware of the CPU.
     */
    private HashMap<Integer, AttachableHardware> attachableHardware = new HashMap<>();

    /**
     * Variables used to reduce memory footprint.
     */
    private int[] hardwareArgs = new int[2];
    private int[] arguments = new int[2];
    private long[] longValues = new long[2]; // Used for operations where the signed 32 bit limit of integers are reached

    /**
     * Constructs the CPU.
     */
    public CPU() {

        memory = new Memory(0x10000);
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

        long startupTime = System.currentTimeMillis();

        while (registers[PC] + instructionCounter < program.length) {

            offset = program[registers[PC] + instructionCounter];

            if (passCycle == 0) {

                executeInstruction(program);
            }

            passCycle -= (passCycle > 0 ? 1 : 0);

            if (!settingProgramCounter) {

                registers[PC]++;
                instructionCounter += (offset - 1);
            }

            settingProgramCounter = false;

            if ((System.currentTimeMillis() - startupTime) > 0xffffffffl) {

                startupTime += 0xffffffffl;
            }

            time = (int) (System.currentTimeMillis() - startupTime);
        }
    }

    /**
     * Executes the instruction.
     */
    private void executeInstruction(int program[]) {

        try {

            int amountOfArguments = (program[registers[PC] + instructionCounter] - 2) / 3;
            int address = -1;
            boolean writeToMemory = false;

            if (arguments.length < amountOfArguments) {

                arguments = new int[amountOfArguments];
            }

            for (int i = 0; i < amountOfArguments; i++) {

                int value                  = program[registers[PC] + instructionCounter + 2 + 3 * i];
                boolean retrieveFromMemory = program[registers[PC] + instructionCounter + 3 + 3 * i] == 1;
                boolean register           = program[registers[PC] + instructionCounter + 4 + 3 * i] == 1;

                if (register) {

                    if (value == PUSH_POP) {

                        // Push
                        if (i == 0 && retrieveFromMemory) {

                            if (registers[SP] - 1 < 0) {

                                fault("Stack overflow.");
                            }

                            writeToMemory = true;

                            address = registers[SP] - 1;
                            arguments[i] = memory.get(registers[SP] - 1);

                            registers[SP]--;
                        }
                        // Pop
                        else if (i == 1) {

                            if (registers[SP] + 1 >= memory.capasity()) {

                                fault("Stack underflow.");
                            }

                            arguments[i] = memory.get(registers[SP]);

                            memory.set(registers[SP], 0);

                            registers[SP]++;
                        }

                        continue;
                    }
                    else if (value == PEEK && retrieveFromMemory) {

                        if (i == 0) {

                            writeToMemory = true;

                            address = registers[SP];

                            arguments[i] = memory.get(registers[SP]);

                            continue;
                        }
                        else if (i == 1) {

                            arguments[i] = memory.get(registers[SP]);

                            continue;
                        }
                    }
                }

                int instruction = program[registers[PC] + instructionCounter + 1];

                if (retrieveFromMemory) {

                    if (i == 0 || (i == 1 && (instruction == HDP || instruction == HRT))) {

                        writeToMemory = true;

                        address = register ? registers[value] : value;
                    }

                    arguments[i] = register ? memory.get(registers[value]) : memory.get(value);
                }
                else {

                    if (i == 0 || (i == 1 && (instruction == HDP || instruction == HRT))) {

                        if (!register && instruction != HDP && instruction != HRT && instruction != JSR) {

                            fault("Invalid argument: " + "'" + value + "'.");
                        }

                        address = value;
                    }

                    arguments[i] = register ? registers[value] : value;
                }
            }

            switch (program[registers[PC] + instructionCounter + 1]) {

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

                    longValues[0] = ((long) arguments[0]) & 0xffffffffl;
                    longValues[1] = ((long) arguments[1]) & 0xffffffffl;

                    if (longValues[0] + longValues[1] > 0xffffffffl) {

                        fault("Overflow.");
                    }

                    longValues[0] += longValues[1];

                    setValue(address, writeToMemory, (int) longValues[0]);

                    break;

                case SUB:

                    longValues[0] = ((long) arguments[0]) & 0xffffffffl;
                    longValues[1] = ((long) arguments[1]) & 0xffffffffl;

                    if (longValues[0] - longValues[1] < 0x0l) {

                        fault("Underflow.");
                    }

                    longValues[0] -= longValues[1];

                    setValue(address, writeToMemory, (int) longValues[0]);

                    break;

                case MUL:

                    longValues[0] = ((long) arguments[0]) & 0xffffffffl;
                    longValues[1] = ((long) arguments[1]) & 0xffffffffl;

                    if (longValues[0] * longValues[1] > 0xffffffffl) {

                        fault("Overflow.");
                    }

                    longValues[0] *= longValues[1];

                    setValue(address, writeToMemory, (int) longValues[0]);

                    break;

                case DIV:

                    if (arguments[1] == 0) {

                        fault("Can't divide by zero.");
                    }
                    else {

                        arguments[0] /= arguments[1];
                    }

                    setValue(address, writeToMemory, arguments[0]);

                    break;

                case MOD:

                    if (arguments[1] == 0) {

                        fault("Can't divide by zero.");
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


                case JSR:

                    registers[SP]--;

                    // TODO: Can cause overflow in extreme conditions

                    memory.set(registers[SP], registers[PC] + 1);

                    setValue(PC, false, arguments[0]);

                    break;

                case HDP:

                    {
                        if (arguments.length - 1 > hardwareArgs.length) {

                            hardwareArgs = new int[arguments.length - 1];
                        }

                        System.arraycopy(arguments, 1, hardwareArgs, 0, arguments.length - 1);

                        int location = arguments[0];

                        if (writeToMemory) {

                            location = memory.get(address);
                        }

                        if (!attachableHardware.containsKey(location)) {

                            fault("Invalid hardware location.");
                        }

                        attachableHardware.get(location).process(hardwareArgs, amountOfArguments - 1);
                    }

                    break;

                case HRT:

                    {
                        if (arguments.length - 1 > hardwareArgs.length) {

                            hardwareArgs = new int[arguments.length - 1];
                        }

                        System.arraycopy(arguments, 1, hardwareArgs, 0, arguments.length - 1);

                        int location = arguments[0];

                        if (!attachableHardware.containsKey(location)) {

                            fault("Invalid hardware location.");
                        }

                        System.out.println(Arrays.toString(hardwareArgs));

                        setValue(address, writeToMemory, attachableHardware.get(location).retrieve(hardwareArgs, amountOfArguments - 1));
                    }

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

        if (address == PC && !writeToMemory) {

            instructionCounter = instructionCounterArray[value] - value;

            settingProgramCounter = true;
        }

        if (writeToMemory) {

            memory.set(address, value);
        }
        else {

            registers[address] = value;
        }
    }

    /**
     * Warns that the current instruction isn't a valid instruction.
     */
    private void fault(String msg) {

        Exit.exit("Instruction fault at " + registers[PC] + ": " + msg);
    }

    /**
     * Dumps the registers.
     */
    public void dumpRegisters() {

        System.out.println(

                "Register A [" + (((long) registers[0]) & 0xffffffffl) + "]" + "\n" +
                "Register B [" + (((long) registers[1]) & 0xffffffffl) + "]" + "\n" +
                "Register C [" + (((long) registers[2]) & 0xffffffffl) + "]" + "\n" +
                "Register X [" + (((long) registers[3]) & 0xffffffffl) + "]" + "\n" +
                "Register Y [" + (((long) registers[4]) & 0xffffffffl) + "]" + "\n" +
                "Register Z [" + (((long) registers[5]) & 0xffffffffl) + "]" + "\n" +
                "Register I [" + (((long) registers[6]) & 0xffffffffl) + "]" + "\n" +
                "Register J [" + (((long) registers[7]) & 0xffffffffl) + "]" + "\n" +
                "PC         [" + (((long) registers[PC]) & 0xffffffffl) + "]" + "\n" +
                "SP         [" + (((long) registers[SP]) & 0xffffffffl) + "]" + "\n" +
                "EX         [" + (((long) registers[EX]) & 0xffffffffl) + "]" + "\n"
        );
    }

    /**
     * Resets the registers.
     */
    private void reset() {

        for (int i = 0; i < registers.length; i++) {

            registers[i] = 0;
        }

        registers[SP] = memory.capasity() - 1;
    }
}
