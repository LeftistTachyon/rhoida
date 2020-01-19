package com.github.leftisttachyon.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
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
public class Instruction {
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
     * The map of button presses and mouse movements to do
     */
    @Setter(value = AccessLevel.NONE)
    private final HashMap<String, String> inputMap;

    /**
     * Creates a new Instruction object.
     *
     * @param inputMap the map of inputs
     */
    public Instruction(HashMap<String, String> inputMap) {
        this.inputMap = inputMap;
    }

    /**
     * Executes this instruction.<br/>
     * <b>NOTE: for the mouse, first mouse click actions are parsed, then mouse movement</b>
     *
     * @param r         the {@link Robot} to execute these instructions with
     * @param preceding the preceding instructions
     */
    public void execute(Robot r, Instruction preceding) {
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
                x = Integer.parseInt(currInput);
            } else if ("MY".equals(key)) {
                y = Integer.parseInt(currInput);
            } else if (key.startsWith("K")) {
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                if (NO_INPUT.contains(currInput)) {
                    switch (key.substring(1)) {
                        case "SHIFT":
                            r.keyRelease(VK_SHIFT);
                            break;
                        case "TAB":
                            r.keyRelease(VK_TAB);
                            break;
                        case "CTRL":
                            r.keyRelease(VK_CONTROL);
                            break;
                        case "ALT":
                            r.keyRelease(VK_ALT);
                            break;
                        case "BACKSPACE":
                            r.keyRelease(VK_BACK_SPACE);
                            break;
                        case "INSERT":
                            r.keyRelease(VK_INSERT);
                            break;
                        case "DELETE":
                            r.keyRelease(VK_DELETE);
                            break;
                        case "UP":
                            r.keyRelease(VK_UP);
                            break;
                        case "LEFT":
                            r.keyRelease(VK_LEFT);
                            break;
                        case "DOWN":
                            r.keyRelease(VK_DOWN);
                            break;
                        case "RIGHT":
                            r.keyRelease(VK_RIGHT);
                            break;
                        case "ENTER":
                            r.keyRelease(VK_ENTER);
                            break;
                        default:
                            if (key.length() == 2) {
                                char c = key.charAt(1);
                                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                                if (keyCode == VK_UNDEFINED) {
                                    throw new IllegalArgumentException("Unknown instruction key: " + key);
                                } else {
                                    r.keyRelease(c);
                                }
                            } else {
                                throw new IllegalArgumentException("Unknown instruction key: " + key);
                            }
                            break;
                    }
                } else {
                    switch (key.substring(1)) {
                        case "SHIFT":
                            r.keyPress(VK_SHIFT);
                            break;
                        case "TAB":
                            r.keyPress(VK_TAB);
                            break;
                        case "CTRL":
                            r.keyPress(VK_CONTROL);
                            break;
                        case "ALT":
                            r.keyPress(VK_ALT);
                            break;
                        case "BACKSPACE":
                            r.keyPress(VK_BACK_SPACE);
                            break;
                        case "INSERT":
                            r.keyPress(VK_INSERT);
                            break;
                        case "DELETE":
                            r.keyPress(VK_DELETE);
                            break;
                        case "UP":
                            r.keyPress(VK_UP);
                            break;
                        case "LEFT":
                            r.keyPress(VK_LEFT);
                            break;
                        case "DOWN":
                            r.keyPress(VK_DOWN);
                            break;
                        case "RIGHT":
                            r.keyPress(VK_RIGHT);
                            break;
                        case "ENTER":
                            r.keyPress(VK_ENTER);
                            break;
                        default:
                            if (key.length() == 2) {
                                char c = key.charAt(1);
                                int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                                if (keyCode == VK_UNDEFINED) {
                                    throw new IllegalArgumentException("Unknown instruction key: " + key);
                                } else {
                                    r.keyPress(c);
                                }
                            } else {
                                throw new IllegalArgumentException("Unknown instruction key: " + key);
                            }
                            break;
                    }
                }
            } else if (key.startsWith("M")) {
                if (prevInput != null && prevInput.equals(currInput)) {
                    continue;
                }

                int button = Integer.parseInt(key.substring(1));
                if (NO_INPUT.contains(key)) {
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
     * {@inheritDoc}
     * @return a {@link String} representation of this {@link Instruction} object.
     */
    @Override
    public String toString() {
        return "[Instruction " + inputMap + ']';
    }
}
