package io.simengangstad.github.cpu.hardware;

import io.simengangstad.github.cpu.AttachableHardware;

import java.util.Arrays;

// TODO: Might leave out identifer and just base information on how many arguments?

/**
 * An identifier is always the first argument. It's used for declaring which information is
 * being dispatched. The rest of the arguments are the values.
 *
 * 0 a          - Sets the address (a) to the pixels in memory.
 * 1 x y v      - Sets the value of v at x y coordinate.
 * 2            - Repaint
 *
 * @author simengangstad
 * @since 11.07.15
 */
public class Monitor extends AttachableHardware {

    /**
     * The pointer to the pixels in memory.
     */
    private int address = 0;

    /**
     * Width and height of monitor. A check is held to ensure that the capasity of the
     * space in memory given is >= width * height.
     */
    private int width, height;

    private Window window;
    private Screen screen;

    /**
     * Initializes the monitor.
     *
     * @param width  The width of the monitor.
     * @param height The height of the monitor.
     */
    public Monitor(int width, int height) {

        this.width = width;
        this.height = height;

        window = Window.setupWindow(960, 560);

        int x = width;
        int y = height;
        int xs = 32;
        int ys = 32;

        screen = new Screen(window.width / 2 - x * xs / 2, window.height / 2 - y * ys / 2, x, y, xs, ys);
    }

    public void repaint() {

        window.clear((byte) 255);
        window.render(screen);
        window.repaint();
    }

    @Override
    public void process(int[] args, int size) throws RuntimeException {

        switch (args[0]) {

            case 0:

                if (size != 2) {

                    throw new RuntimeException("Incorrect amount of arguments for setting address to pixels.");
                }

                address = args[1];

                if (address + width * height >= memory.capasity()) {

                    throw new RuntimeException("Not enough allocated space at address " + address + " for " + (width * height) + " pixels.");
                }

                break;

            case 1:

                if (size != 4) {

                    throw new RuntimeException("Incorrect amount of arguments for setting value to pixels.");
                }

                if (args[1] < 0 || args[1] >= width || args[2] < 0 || args[2] >= height) {

                    throw new RuntimeException("Invalid coordinate while setting pixel: " + args[1] + ", " + args[2]);
                }


                memory.set(address + args[1] + args[2] * width, args[3]);
                screen.pixels[args[1] + args[2] * width] = args[3];

                break;

            case 2:

                repaint();

                break;

            default:

                throw new RuntimeException("Unknown identifier.");
        }
    }
}
