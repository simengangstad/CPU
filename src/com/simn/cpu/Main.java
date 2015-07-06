package com.simn.cpu;

import com.simn.cpu.exception.AssembleException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        CPU cpu = new CPU();

        Assembler assembler = new Assembler();

        // All comunication to other hardware will be done with the hardware functions, this includes memory

        StringBuilder stringBuilder = new StringBuilder();

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader("res/test"));

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

            assembledCode = assembler.assemble(code, true);
        }
        catch (AssembleException assembledException) {

            System.err.println(assembledException.getMessage());

            return;
        }

        System.out.println("\nByte code:");

        for (int i = 0; i < assembledCode.length; i++) {

            System.out.println(i + ". " + assembledCode[i]);
        }

        System.out.println();

        cpu.execute(assembledCode);
        cpu.dumpRegisters();


        System.out.println();

        //cpu.memory.dump();
    }

    public static String toBinaryString(byte value) {

        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(" ", "0");
    }
}