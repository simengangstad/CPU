package io.simengangstad.github.test.g2d.io;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

    private static boolean[] keys = new boolean[256];

    private static boolean[] lastKeys = new boolean[256];

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        Keyboard.keys[keyEvent.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        Keyboard.keys[keyEvent.getKeyCode()] = false;
    }

    public boolean isKeyPressed(int key) {
        return Keyboard.keys[key];
    }

    public boolean isKeyTyped(int key) {
        return Keyboard.keys[key] && !Keyboard.lastKeys[key];
    }

    public boolean isKeyReleased(int key) {
        return !Keyboard.keys[key];
    }

    public void poll() {
        System.arraycopy(Keyboard.keys, 0, Keyboard.lastKeys, 0, Keyboard.lastKeys.length);
    }
}
