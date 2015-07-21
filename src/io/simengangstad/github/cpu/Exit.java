package io.simengangstad.github.cpu;

/**
 * @author simengangstad
 * @since 11.07.15
 */
public class Exit {

    public static void exit(String msg) {

        System.err.println(msg);

        System.exit(-1);
    }
}
