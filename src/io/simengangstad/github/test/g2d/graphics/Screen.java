package io.simengangstad.github.test.g2d.graphics;

public class Screen {

    public final int x, y, width, height, xs, ys;

    public final int[] pixels;

    public Screen(int x, int y, int width, int height, int xs, int ys) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xs = xs;
        this.ys = ys;

        pixels = new int[width * height];
    }
}
