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
        this(component, LEFT_ALIGNMENT);
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

        updateFrameNums();
    }

    /**
     * Creates a new {@link FrameLineNumber}
     *
     * @param component the {@link JTextComponent to pay attention to}
     * @param alignment the direction of alignment
     */
    public FrameLineNumber(JTextComponent component, float alignment) {
        this(component, 4, alignment);
    }

    /**
     * Updates {@code frameNums}
     */
    public void updateFrameNums() {
        frameNums.clear();

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

        LinkedList<Integer> weights = new LinkedList<>();
        ArrayList<Integer> vals = new ArrayList<>();

        frameNums.add(0);
        weights.add(1);
        vals.add(0);
        for (int i = 1; i < lines.length; i++) {
            log.trace("@{}: frameNums: {}", i, frameNums);
            log.trace("@{}: weights  : {}", i, weights);
            log.trace("@{}: vals     : {}", i, vals);

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

            int indentLevel = weights.size() - 1;
            int localIndent = firstNonSpace / 4;
            if (localIndent > indentLevel) {
                log.trace("Jumping indentation");
                frameNums.add(-1);
                continue;
            } else if (localIndent != indentLevel) {
                log.trace("@{}: Dropping down from {} to {}", i, indentLevel, localIndent);

                for (int j = 0, size = vals.size() - 1; j < indentLevel - localIndent; j++) {
                    weights.removeLast();
                    vals.remove(size - j);
                }
            }

            int weight = weights.getLast();

            if (content.startsWith("INCLUDE ")) {
                // damn
                log.trace("INCLUDE detected");
                frameNums.add(parentPath == null
                        ? 0
                        : getFrameNums(Paths.get(parentPath, content.substring(8)).toFile()));
            } else {
                if (content.startsWith("REPEAT ")) {
                    log.trace("REPEAT detected @{}", i);
                    String num = content.substring(7);
                    if (!num.matches("\\d+")) {
                        frameNums.add(-1);
                        continue;
                    }

                    int mult = Integer.parseInt(num);
                    weights.add(mult * weight);
                    frameNums.add(0);
                    vals.add(vals.get(vals.size() - 1));
                } else {
                    log.trace("Standard procedures");
                    int j = vals.size() - 1;
                    for (Iterator<Integer> iter = weights.iterator(); iter.hasNext(); j--) {
                        vals.set(j, vals.get(j) + iter.next());
                    }
                    frameNums.add(vals.get(vals.size() - 1));
                }
            }
        }

        log.trace("@end: frameNums: {}", frameNums);
        log.trace("@end: weights  : {}", weights);
        log.trace("@end: vals     : {}", vals);
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
            log.trace("Calling getNumber({}) yielded \"\"", idx);
            return "";
        } else if (get == -1) {
            log.trace("Calling getNumber({}) yielded \"ERR\"", idx);
            return "ERR";
        } else {
            log.trace("Calling getNumber({}) yielded \"{}\"", idx, get);
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
