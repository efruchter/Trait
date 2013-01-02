package efruchter.tp.util;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

/**
 * LWJGL keyboard wrapper that lets you track keyPressed, keyDown events. Also
 * prevents missing events due to bad polling.
 * 
 * @author toriscope
 * 
 */
public class KeyUtil {

    private static final Set<Integer> keyDown = new HashSet<Integer>();
    private static final Set<Integer> keyJustPressed = new HashSet<Integer>();

    /**
     * Run this update at the start of the update loop, prior to polling.
     */
    public static void update() {

        if (!keyJustPressed.isEmpty())
            keyJustPressed.clear();

        while (Keyboard.next()) {
            final Integer key = Keyboard.getEventKey();
            if (Keyboard.getEventKeyState()) {
                keyDown.add(key);
                keyJustPressed.add(key);
            } else {
                keyDown.remove(key);
            }
        }
    }

    public static boolean isKeyPressed(final int key) {
        return keyJustPressed.contains(key);
    }

    public static boolean isKeyDown(final int key) {
        return keyDown.contains(key);
    }

    private KeyUtil() {
    }
}
