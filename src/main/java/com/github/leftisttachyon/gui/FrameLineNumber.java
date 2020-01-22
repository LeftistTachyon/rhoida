package com.github.leftisttachyon.gui;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Creates a class that keeps track of frame numbers on the right.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public class FrameLineNumber extends TextLineNumber implements ChangeListener {
    /**
     * A cache for the file sizes
     */
    private static final HashMap<File, Integer> SIZE_CACHE = new HashMap<>();
    /**
     * The {@link ArrayList} that contains the frame numbers
     */
    private ArrayList<Integer> frameNums;
    /**
     * A {@link TreeMap} that contains the line weights and indentation levels.<br/>
     * The first element of the value is the weight, and the second element of the value is the indentation level.
     */
    private TreeMap<Integer, int[]> weights;
    /**
     * The path of the parent file
     */
    @Setter
    private String parentPath = null;

    /**
     * Creates a new {@link FrameLineNumber}
     *
     * @param component the {@link JTextComponent} to pay attention to
     */
    public FrameLineNumber(JTextComponent component) {
        this(component, 3, LEFT_ALIGNMENT);
    }

    /**
     * Creates a new {@link FrameLineNumber}
     *
     * @param component            the {@link JTextComponent} to pay attention to
     * @param minimumDisplayDigits the minimum number of digits to display
     * @param alignment            the direction of alignment
     */
    public FrameLineNumber(JTextComponent component, int minimumDisplayDigits, float alignment) {
        super(component, minimumDisplayDigits, alignment);

        frameNums = new ArrayList<>();
        weights = new TreeMap<>();

        updateFrameNums();
    }

    /**
     * Creates a new {@link FrameLineNumber}
     *
     * @param component the {@link JTextComponent to pay attention to}
     * @param alignment the direction of alignment
     */
    public FrameLineNumber(JTextComponent component, float alignment) {
        this(component, 3, alignment);
    }

    /**
     * Updates {@code frameNums}
     */
    public void updateFrameNums() {
        frameNums.clear();
        weights.clear();

        String[] lines = super.component.getText().split("\n");
        log.trace("lines: {}", Arrays.toString(lines));
        if (lines.length == 0) {
            return;
        }

        if (!lines[0].startsWith("!FORMAT: ")) {
            for (int i = 0; i < lines.length; i++) {
                frameNums.add(-1);
            }
            log.debug("Bad header");
            return;
        }

        frameNums.add(0);
        weights.put(0, new int[]{1, 0});

        for (int i = 1, trueVal = 0, shownVal = 0; i < lines.length; i++) {
            log.trace("@{}: frameNums: {}", i, frameNums);
            log.trace("@{}: weights  :", i);
            if (log.isTraceEnabled()) {
                for (Map.Entry<Integer, int[]> entry : weights.entrySet()) {
                    log.trace("{} = {}", entry.getKey(), Arrays.toString(entry.getValue()));
                }
            }

            String line = lines[i].replace("\t", "    ");
            String content = line.stripLeading();
            int firstNonSpace = line.length() - content.length();
            if (line.length() == 0 || content.startsWith("#")) {
                frameNums.add(0);
                continue;
            }
            if (firstNonSpace % 4 != 0) {
                log.trace("Bad indentation");
                frameNums.add(-1);
                continue;
            }

            Map.Entry<Integer, int[]> floor = weights.floorEntry(i);
            int[] val = floor.getValue();
            int localIndent = firstNonSpace / 4;
            if (localIndent > val[1]) {
                log.trace("Jumping indentation");
                frameNums.add(-1);
                continue;
            } else if (localIndent != val[1]) {
                log.trace("@{}: Dropping down from {} to {}", i, val[1], localIndent);

                SortedMap<Integer, int[]> before = weights.descendingMap().tailMap(i);
                int[] newVal = null;
                for (Map.Entry<Integer, int[]> entry : before.entrySet()) {
                    int[] value = entry.getValue();
                    log.trace("Checking @level {} \\w indent 'o {} ?= {}", entry.getKey(), value[1], localIndent);
                    if (value[1] == localIndent) {
                        newVal = value;
                        break;
                    }
                }

                log.trace("newVal: {}", Arrays.toString(newVal));

                shownVal = trueVal;
                weights.put(i + 1, newVal);
            }

            if (content.startsWith("INCLUDE ")) {
                // damn
                log.trace("INCLUDE detected");
                frameNums.add(parentPath == null
                        ? 0
                        : getFrameNums(Paths.get(parentPath, content.substring(8)).toFile()));
            } else {
                val = weights.floorEntry(i).getValue();
                if (content.startsWith("REPEAT ")) {
                    log.trace("REPEAT detected @{}", i);
                    String num = content.substring(7);
                    if (!num.matches("\\d+")) {
                        frameNums.add(-1);
                        continue;
                    }
                    int mult = Integer.parseInt(num);
                    int[] newVal = new int[]{val[0] * mult, val[1] + 1};
                    log.trace("newVal: {}", Arrays.toString(val));
                    weights.put(i + 1, newVal);
                    frameNums.add(0);
                } else {
                    log.trace("Standard procedures");
                    trueVal += val[0];
                    frameNums.add(++shownVal);
                }
            }
        }

        log.trace("@end: frameNums: {}", frameNums);
        log.trace("@end: weights  :");
        if (log.isTraceEnabled()) {
            for (Map.Entry<Integer, int[]> entry : weights.entrySet()) {
                log.trace("{} = {}", entry.getKey(), Arrays.toString(entry.getValue()));
            }
        }
    }

    private int getFrameNums(File file) {
        if (SIZE_CACHE.containsKey(file)) {
            return SIZE_CACHE.get(file);
        }
        try (BufferedReader in = Files.newBufferedReader(file.toPath())) {
            int cnt = countIndented(in, 0, file.getParent());
            if (cnt != -1) SIZE_CACHE.put(file, cnt);
            return cnt;
        } catch (IOException e) {
            log.warn("An IOException was thrown while reading the file", e);
            return -1;
        }
    }

    private int countIndented(BufferedReader in, int indentationLevel, String filePath) throws IOException {
        int output = 0;
        String line;
        while ((line = in.readLine()) != null) {
            line = line.replace("\t", "    ");
            String content = line.stripLeading();
            log.trace("content: {}", content);

            if (line.isBlank() || content.startsWith("#")) {
                continue;
            }

            int firstNonSpace = line.length() - content.length();
            if (firstNonSpace % 4 != 0 || firstNonSpace / 4 > indentationLevel) {
                return -1;
            }

            log.trace("Indentation math: compare {} to {}", firstNonSpace / 4, indentationLevel);

            if (firstNonSpace / 4 < indentationLevel) {
                in.reset();

                break;
            }

            // guaranteed: indentationLevel * 4 == firstNonSpace
            if (content.startsWith("INCLUDE ")) {
                Path path = Paths.get(filePath, content.substring(8));
                log.trace("Fragment path: {}", path);
                output += getFrameNums(path.toFile());
            } else if (content.startsWith("REPEAT ")) {
                int repeat = Integer.parseInt(content.substring(7));
                int counted = countIndented(in, indentationLevel + 1, filePath);

                output += repeat * counted;
            } else {
                output++;
            }

            in.mark(1_000);
        }

        return output;
    }

    @Override
    protected String getNumber(int idx) {
        if (idx >= frameNums.size()) {
            return "";
        }

        int get = frameNums.get(idx);
        if (get == 0) {
            log.debug("Calling getNumber({}) yielded \"\"", idx);
            return "";
        } else if (get == -1) {
            log.debug("Calling getNumber({}) yielded \"ERR\"", idx);
            return "ERR";
        } else {
            log.debug("Calling getNumber({}) yielded \"{}\"", idx, get);
            return String.valueOf(get);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(SIZE_CACHE::clear);
    }

    @Override
    public void paintComponent(Graphics g) {
        updateFrameNums();

        super.paintComponent(g);
    }
}
