package io.simengangstad.github.test.cpu;


import io.simengangstad.github.test.g2d.GameListener;
import io.simengangstad.github.test.g2d.graphics.Screen;

import java.util.Arrays;

/**
 * @author simengangstad
 * @since 11.07.15
 */
public class Main {

    public static class Game extends GameListener {

        Screen cpu;

        public Game() {

            super("CPU", 960, 560);

            int x = 16;
            int y = 16;
            int xs = 24;
            int ys = 24;

            cpu = new Screen(window.getWidth() / 2 - x * xs / 2, window.getHeight() / 2 - y * ys / 2, x, y, xs, ys);

            Arrays.fill(cpu.pixels, 0xffffff);
        }

        @Override
        public void init() {

        }

        @Override
        public void tick(double delta) {

        }

        @Override
        public void render() {

            window.clear(0, 0, 0);
            window.render(cpu);
        }
    }

    public static void main(String[] args) {

        new Game();
    }
}

