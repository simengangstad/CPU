package io.simengangstad.github.test.g2d;


import io.simengangstad.github.test.g2d.graphics.Window;

public abstract class GameListener {

    protected Window window;

    public GameListener(String title, int width, int height) {

        window = new Window(this, title, width, height);

        window.start();
    }

    public abstract void init();

    public abstract void tick(double delta);

    public abstract void render();
}
