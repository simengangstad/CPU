package io.simengangstad.github.cpu;

import io.simengangstad.github.cpu.exception.AssembleException;
import io.simengangstad.github.cpu.hardware.Monitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        CPU cpu = new CPU();
        cpu.attachHardware(0, new Monitor(48, 32));


        Assembler assembler = new Assembler();

        // All comunication to other hardware will be done with the hardware functions, this includes memory

        StringBuilder stringBuilder = new StringBuilder();

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader("res/draw_loop"));

            String line;

            while ((line = bufferedReader.readLine()) != null) {

                stringBuilder.append(line).append("\n");
            }

            bufferedReader.close();
        }
        catch (IOException exception) {

            exception.printStackTrace();
        }


        String code = stringBuilder.toString();

        int[] assembledCode;

        try {

            assembledCode = assembler.assemble(code, false);
        }
        catch (AssembleException assembledException) {

            System.err.println(assembledException.getMessage());

            return;
        }
/*
        System.out.println("\nByte code:");

        for (int i = 0; i < assembledCode.length; i++) {

            System.out.println(i + ". " + "0x" + Integer.toHexString(assembledCode[i]));
        }

        System.out.println();*/
        cpu.execute(assembledCode);
        cpu.dumpRegisters();


        cpu.memory.dump(0, 10);
    }
}