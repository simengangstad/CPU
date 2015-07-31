package io.simengangstad.github.cpu.hardware;

import io.simengangstad.github.cpu.AttachableHardware;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * An identifier is always the first argument. It's used for declaring which information is
 * being dispatched. The rest of the arguments are the values.
 *
 * Processing:
 *
 * 0 a          - Sets the address (a) to the pixels in memory.
 * 1 x y v      - Sets the value of v at x y coordinate.
 * 2            - Flushes the pixels.
 * 3            - Clear to black.
 *
 * Retrieving:
 *
 * 0            - Returns the width.
 * 1            - Returns the height.
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
        int xs = 480 / width;
        int ys = 320 / height;

        screen = new Screen(window.width / 2 - x * xs / 2, window.height / 2 - y * ys / 2, x, y, xs, ys);
    }

    public void repaint() {

        memory.copyInto(address, screen.pixels, 0, width * height);

        window.render(screen);
        window.repaint();
    }

    @Override
    public void process(int[] arguments, int size) throws RuntimeException {

        switch (arguments[0]) {

            case 0:

                if (size != 2) {

                    throw new RuntimeException("Incorrect amount of arguments for setting address to pixels.");
                }

                address = arguments[1];

                if (address + width * height > memory.capasity()) {

                    throw new RuntimeException("Not enough allocated space at address " + address + " for " + (width * height) + " pixels.");
                }

                break;

            case 1:

                if (size != 4) {

                    throw new RuntimeException("Incorrect amount of arguments for setting value to pixels.");
                }

                if (arguments[1] < 0 || arguments[1] >= width || arguments[2] < 0 || arguments[2] >= height) {

                    throw new RuntimeException("Invalid coordinate while setting pixel: " + arguments[1] + ", " + arguments[2]);
                }


                memory.set(address + arguments[1] + arguments[2] * width, arguments[3]);

                break;

            case 2:

                if (size != 1) {

                    throw new RuntimeException("Incorrect amount of arguments for flush.");
                }

                repaint();

                break;

            case 3:

                if (size != 1) {

                    throw new RuntimeException("Incorrect amount of arguments for clearing to black.");
                }

                for (int i = 0; i < width * height; i++) {

                    memory.set(i, 0x0);
                }

                repaint();

                break;

            default:

                throw new RuntimeException("Unknown identifier.");
        }
    }

    @Override
    public int retrieve(int[] arguments, int size) throws RuntimeException {

        switch (arguments[0]) {

            case 0x0:

                if (size != 2) {

                    throw new RuntimeException("Incorrect amount of arguments for retrieving width.");
                }

                return width;

            case 0x1:

                if (size != 2) {

                    throw new RuntimeException("Incorrect amount of arguments for retrieving height.");
                }

                return height;

            default:

                return 0;
        }
    }
}
