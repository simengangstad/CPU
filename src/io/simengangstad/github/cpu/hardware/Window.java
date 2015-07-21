package io.simengangstad.github.cpu.hardware;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * @author simengangstad
 * @since 11.07.15
 */
public class Window extends JPanel {

    public final int width, height;

    /*
    public Keyboard keyboard;
    public Mouse mouse;
*/

    private BufferedImage bufferedImage;
    private int[] pixels;


    public static Window setupWindow(int width, int height) {

        final Window window = new Window(width, height);

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(window, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
        });

        return window;
    }

    private Window(int width, int height) {

        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width, height));

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

        /*
        keyboard = new Keyboard();
        mouse = new Mouse();


        addKeyListener(keyboard);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);*/
    }



    @Override
    protected void paintComponent(Graphics g) {

        g.drawImage(bufferedImage, 0, 0, width, height, null);
    }

    /*

    private void poll() {

        keyboard.poll();
        mouse.poll();
    }
    */

    public void setPixel(int x, int y, int value) {

        if ((x < 0 || x >= width) || (y < 0 || y >= height)) {

            return;
        }

        pixels[x + y * width] = value;
    }

    public void clear(int value) {

        Arrays.fill(pixels, value);
    }

    public void render(Screen screen) {

        /*
        if (((screen.x + screen.xs * screen.width < 0) || (screen.x > width)) || ((screen.y + screen.height * screen.ys < 0) || (screen.y > height)))
        {
            return;
        }

*/

        for (int i = 0; i < screen.height * screen.ys; i++) {

            for (int j = 0; j < screen.width * screen.xs; j++) {

                setPixel(j + screen.x, i + screen.y, screen.pixels[j / screen.xs + (i / screen.ys) * screen.width]);
            }
        }
    }
}
