package io.simengangstad.github.test.g2d.io;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Mouse implements MouseListener, MouseMotionListener {

    private static int x, y;

    private static boolean[] buttons = new boolean[5];

    private static boolean[] lastButtons = new boolean[5];

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        Mouse.buttons[mouseEvent.getButton()] = true;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        Mouse.buttons[mouseEvent.getButton()] = false;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        Mouse.x = mouseEvent.getX();
        Mouse.y = mouseEvent.getY();
    }

    public boolean isButtonPressed(int button) {
        return Mouse.buttons[button];
    }

    public boolean isButtonClicked(int button) {
        return Mouse.buttons[button] && !Mouse.lastButtons[button];
    }

    public boolean isButtonReleased(int button) {
        return !Mouse.buttons[button];
    }

    public int getX() {
        return Mouse.x;
    }

    public int getY() {
        return Mouse.y;
    }

    public void poll() {
        System.arraycopy(Mouse.buttons, 0, Mouse.lastButtons, 0, Mouse.lastButtons.length);
    }
}
