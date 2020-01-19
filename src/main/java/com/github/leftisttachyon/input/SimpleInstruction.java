package com.github.leftisttachyon.input;

import com.github.leftisttachyon.input.compiled.CompiledInstruction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.awt.event.KeyEvent.*;

/**
 * A class that represents a set of key presses and mouse movements and presses.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
@Data
@Setter(AccessLevel.NONE)
public class SimpleInstruction {
    /**
     * A {@link Set} of {@link String}s that denote "no key press here."
     */
    private static final Set<String> NO_INPUT = Set.of(".", "_", "-");
    /**
     * The offset for the mouse in the x-direction
     */
    @Getter
    @Setter
    private static int X_OFFSET = 0;
    /**
     * The offset for the mouse in the y-direction
     */
    @Getter
    @Setter
    private static int Y_OFFSET = 0;

    /**
     * Sets the mouse offset for instruction execution
     *
     * @param p a {@link Point} object that represents the offsets to use
     */
    public static void setOffset(Point p) {
        X_OFFSET = p.x;
        Y_OFFSET = p.y;
    }

    /**
     * Gets the mouse offset for instruction execution
     *
     * @return a {@link Point} object that represents the offsets being used
     */
    public static Point getOffset() {
        return new Point(X_OFFSET, Y_OFFSET);
    }

    /**
     * The map of button presses and mouse movements to do
     */
    private final HashMap<String, String> inputMap;

    /**
     * Executes this instruction.<br/>
     * <b>NOTE: for the mouse, first mouse click actions are parsed, then mouse movement</b>
     *
     * @param r         the {@link Robot} to execute these instructions with
     * @param preceding the preceding instructions
     */
    public void execute(Robot r, SimpleInstruction preceding) {
        if (preceding != null &&
                !preceding.inputMap.keySet().equals(inputMap.keySet())) {
            throw new IllegalArgumentException("Invalid preceding instructions");
        }

        int x = -1, y = -1;
        for (Map.Entry<String, String> inputEntry : inputMap.entrySet()) {
            String key = inputEntry.getKey(),
                    currInput = inputEntry.getValue(),
                    prevInput = preceding == null ? null : preceding.inputMap.get(key);

            if ("MX".equals(key)) {
                if (!NO_INPUT.contains(currInput)) {
                    x = Integer.parseInt(currInput);
                }
            } else if ("MY".equals(key)) {
                if (!NO_INPUT.contains(currInput)) {
                    y = Integer.parseInt(currInput);
                }
            } else if (key.startsWith("K")) {
                log.trace("{} -> {}", prevInput, currInput);
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                int keyCode = getKeyCode(key.substring(1));
                log.trace("Dealing with {} / keycode {}", key, keyCode);
                if (NO_INPUT.contains(currInput)) {
                    if (prevInput == null || NO_INPUT.contains(prevInput)) {
                        log.trace("Continuing");
                        continue;
                    }

                    r.keyRelease(keyCode);
                } else {
                    r.keyPress(keyCode);
                }
            } else if (key.startsWith("M")) {
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                int button = Integer.parseInt(key.substring(1));
                if (NO_INPUT.contains(currInput)) {
                    if (prevInput == null || NO_INPUT.contains(prevInput)) {
                        log.trace("Continuing: {} -> {}", prevInput, currInput);
                        continue;
                    }

                    r.mouseRelease(button);
                } else {
                    r.mousePress(button);
                }
            } else {
                throw new IllegalArgumentException("Unknown instruction key: " + key);
            }
        }

        log.trace("x: {}, y: {}", x, y);

        if (x != -1 && y != -1) {
            r.mouseMove(X_OFFSET + x, Y_OFFSET + y);
        }
    }

    /**
     * Compiles this instruction.
     *
     * @param preceding the preceding instructions
     * @return a compiled version of these instructions.
     * @see CompiledInstruction
     */
    public CompiledInstruction compile(SimpleInstruction preceding) {
        if (preceding != null &&
                !preceding.inputMap.keySet().equals(inputMap.keySet())) {
            throw new IllegalArgumentException("Invalid preceding instructions: " + toString() + " vs "
                    + preceding.toString());
        }

        int x = -1, y = -1;
        Set<Integer> mP = new HashSet<>(), mR = new HashSet<>(),
                kP = new HashSet<>(), kR = new HashSet<>();

        for (Map.Entry<String, String> inputEntry : inputMap.entrySet()) {
            String key = inputEntry.getKey(),
                    currInput = inputEntry.getValue(),
                    prevInput = preceding == null ? null : preceding.inputMap.get(key);

            if ("MX".equals(key)) {
                if (!NO_INPUT.contains(currInput)) {
                    x = Integer.parseInt(currInput);
                }
            } else if ("MY".equals(key)) {
                if (!NO_INPUT.contains(currInput)) {
                    y = Integer.parseInt(currInput);
                }
            } else if (key.startsWith("K")) {
                log.trace("{} -> {}", prevInput, currInput);
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                int keyCode = getKeyCode(key.substring(1));
                log.trace("Dealing with {} / keycode {}", key, keyCode);
                if (NO_INPUT.contains(currInput)) {
                    if (prevInput == null || NO_INPUT.contains(prevInput)) {
                        log.trace("Continuing");
                        continue;
                    }

                    kR.add(keyCode);
                } else {
                    kP.add(keyCode);
                }
            } else if (key.startsWith("M")) {
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                int button = Integer.parseInt(key.substring(1));
                if (NO_INPUT.contains(currInput)) {
                    if (prevInput == null || NO_INPUT.contains(prevInput)) {
                        log.trace("Continuing: {} -> {}", prevInput, currInput);
                        continue;
                    }

                    mR.add(button);
                } else {
                    mP.add(button);
                }
            } else {
                throw new IllegalArgumentException("Unknown instruction key: " + key);
            }
        }

        log.trace("x: {}, y: {}", x, y);

        Point p;
        if (x != -1 && y != -1) {
            p = new Point(X_OFFSET + x, Y_OFFSET + y);
        } else {
            p = null;
        }

        return new CompiledInstruction(p, mP.isEmpty() ? null : mP, mR.isEmpty() ? null : mR,
                kP.isEmpty() ? null : kP, kR.isEmpty() ? null : kR);
    }

    /**
     * From the given word representation of a key, determines and returns the keycode
     *
     * @param key the word representation of a key
     * @return the associated keycode
     */
    private int getKeyCode(String key) {
        switch (key) {
            case "SHIFT":
                return VK_SHIFT;
            case "TAB":
                return VK_TAB;
            case "CTRL":
                return VK_CONTROL;
            case "ALT":
                return VK_ALT;
            case "BACKSPACE":
                return VK_BACK_SPACE;
            case "INSERT":
                return VK_INSERT;
            case "DELETE":
                return VK_DELETE;
            case "UP":
                return VK_UP;
            case "LEFT":
                return VK_LEFT;
            case "DOWN":
                return VK_DOWN;
            case "RIGHT":
                return VK_RIGHT;
            case "ENTER":
                return VK_ENTER;
            default:
                if (key.length() == 1) {
                    char c = key.charAt(0);
                    int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                    if (keyCode == VK_UNDEFINED) {
                        throw new IllegalArgumentException("Unknown instruction key: " + key);
                    } else {
                        return keyCode;
                    }
                } else {
                    throw new IllegalArgumentException("Unknown instruction key: " + key);
                }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return a {@link String} representation of this {@link SimpleInstruction} object.
     */
    @Override
    public String toString() {
        return "[SimpleInstruction " + inputMap + ']';
    }
}
