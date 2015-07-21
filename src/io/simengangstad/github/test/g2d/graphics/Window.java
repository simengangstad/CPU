package io.simengangstad.github.test.g2d.graphics;

import io.simengangstad.github.test.g2d.GameListener;
import io.simengangstad.github.test.g2d.io.Keyboard;
import io.simengangstad.github.test.g2d.io.Mouse;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;


public class Window extends Canvas implements Runnable {

    private JFrame frame;

    private int width, height;

    public Keyboard keyboard;

    public Mouse mouse;

    private final GameListener gameListener;

    private Thread thread;

    private boolean running = false;

    private BufferedImage bufferedImage;

    private Graphics graphics;

    private int[] pixels;

    private int ticks;

    private int fps;

    public Window(GameListener gameListener, String title, int width, int height) {

        this.gameListener = gameListener;

        this.width = width;
        this.height = height;

        super.setPreferredSize(new Dimension(width, height));

        frame = new JFrame(title);
    }

    @Override
    public int getWidth() {

        return width;
    }

    @Override
    public int getHeight() {

        return height;
    }

    public int getTicks() {
        return this.ticks;
    }

    public int getFps() {
        return this.fps;
    }

    public void start() {

        if (running) {

            stop();
        }

        thread = new Thread(this);
        thread.start();
    }

    public void stop() {

        running = false;

        try {

            thread.join();
        }
        catch (InterruptedException interruptedException) {

            interruptedException.printStackTrace();
        }
    }

    @Override
    public void run() {

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (getBufferStrategy() == null) {

            createBufferStrategy(3);
        }

        resize();

        keyboard = new Keyboard();
        mouse = new Mouse();

        addKeyListener(keyboard);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        gameListener.init();

        requestFocus();

        running = true;

        long lastFrame = System.nanoTime();

        long frameTime = 0;

        int ticks = 0;

        final double NS_PER_TICK = 1000000000.0 / 60.0;

        while (running) {

            long now = System.nanoTime();

            long passedTime = now - lastFrame;

            frameTime += passedTime;

            lastFrame = now;

            tick(passedTime / 1000000000.0);
            render();

            ticks++;

            if (frameTime >= 1000000000) {

                this.ticks = ticks;
                this.fps = ticks;

                ticks = 0;

                frameTime = 0;
            }

            long sleepTime = (long) NS_PER_TICK - (System.nanoTime() - now);

            try {

                if (sleepTime < 0) {

                    continue;
                }

                Thread.sleep(sleepTime / 1000000);
            }
            catch (InterruptedException interruptedException) {

                interruptedException.printStackTrace();
            }
        }

        stop();
    }

    private void tick(double delta) {

        if (width != super.getWidth() || height != super.getHeight()) {

            resize();
        }

        gameListener.tick(delta);

        keyboard.poll();
        mouse.poll();
    }

    public void setPixel(int x, int y, int value) {

        if ((x < 0 || x >= width) || (y < 0 || y >= height)) {

            return;
        }

        pixels[x + y * width] = value;
    }

    public void clear(int red, int green, int blue) {

        int value = 0;

        value = value | (red << 16);
        value = value | (green << 8);
        value = value | (blue);

        Arrays.fill(pixels, value);
    }

    public void render(Screen screen) {

        /*
        if (((screen.x + (screen.xs * screen.width) + getWidth() / 2 < 0) || (screen.x + getWidth() / 2 > getWidth())) || ((-screen.y + getHeight() / 2 < 0) || (-screen.y - (screen.ys * getHeight()) + getHeight() / 2 > getHeight())))
        {
            return;
        }
        */

        for (int i = 0; i < screen.width * screen.ys; i++) {

            for (int j = 0; j < screen.width * screen.xs; j++) {

                setPixel(j + screen.x, i + screen.y, screen.pixels[j / screen.xs + (i / screen.ys) * screen.width]);
            }
        }
    }

    private void resize() {

        width = super.getWidth();
        height = super.getHeight();

        pixels = new int[width * height];

        if (bufferedImage != null) {

            bufferedImage.flush();
        }

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
    }

    private void render() {

        BufferStrategy bufferStrategy = super.getBufferStrategy();

        graphics = bufferStrategy.getDrawGraphics();

        gameListener.render();

        flush();

        bufferStrategy.show();
    }

    private void flush() {

        graphics.drawImage(bufferedImage, 0, 0, width, height, null);
        graphics.dispose();
    }
}
