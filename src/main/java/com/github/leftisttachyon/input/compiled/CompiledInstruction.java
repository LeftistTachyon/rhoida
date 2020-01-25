package com.github.leftisttachyon.input.compiled;

import com.github.leftisttachyon.input.SimpleInstruction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Set;

/**
 * A class that represents a compiled {@link SimpleInstruction}
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
@Data
@Setter(AccessLevel.NONE)
public class CompiledInstruction {
    /**
     * The coordinates for the mouse to go to
     */
    private final Point mouseCoords;
    /**
     * The mouse buttons to be pressed
     */
    private final Set<Integer> mousePress;
    /**
     * The mouse buttons to be released
     */
    private final Set<Integer> mouseRelease;
    /**
     * The keyboard buttons to be pressed
     */
    private final Set<Integer> keyPress;
    /**
     * The keyboard buttons to be released
     */
    private final Set<Integer> keyRelease;

    /**
     * Executes this {@link CompiledInstruction}.
     *
     * @param r the {@link Robot} object to execute these instructions with
     */
    public void execute(Robot r) {
        if (mouseCoords != null) {
            r.mouseMove(mouseCoords.x, mouseCoords.y);
        }

        if (mousePress != null && !mousePress.isEmpty()) {
            for (int i : mousePress) {
                log.debug("Invalid? {}", i);
                r.mousePress(i);
            }
        }
        if (mouseRelease != null && !mouseRelease.isEmpty()) {
            for (int i : mouseRelease) {
                r.mouseRelease(i);
            }
        }

        if (keyPress != null && !keyPress.isEmpty()) {
            for (int i : keyPress) {
                r.keyPress(i);
            }
        }
        if (keyRelease != null && !keyRelease.isEmpty()) {
            for (int i : keyRelease) {
                r.keyRelease(i);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[CompiledInstruction");
        if (mouseCoords != null) {
            output.append(" mouseCoords=(");
            output.append(mouseCoords.x);
            output.append(", ");
            output.append(mouseCoords.y);
            output.append(")");
        }
        if (mousePress != null && !mousePress.isEmpty()) {
            output.append(" mousePress=");
            output.append(mousePress);
        }
        if (mouseRelease != null && !mouseRelease.isEmpty()) {
            output.append(" mouseRelease=");
            output.append(mouseRelease);
        }
        if (keyPress != null && !keyPress.isEmpty()) {
            output.append(" keyPress=");
            output.append(keyPress);
        }
        if (keyRelease != null && !keyRelease.isEmpty()) {
            output.append(" keyRelease=");
            output.append(keyRelease);
        }
        output.append(']');

        return output.toString();
    }
}
