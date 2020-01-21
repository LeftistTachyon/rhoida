package com.github.leftisttachyon.input;

import com.github.leftisttachyon.input.compiled.CompiledInstruction;
import com.github.leftisttachyon.input.compiled.CompiledPlayback;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that represents a collection of instructions to execute.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public class SimplePlayback implements Iterable<SimpleInstruction> {
    /**
     * A cache to store {@link SimplePlayback} objects to prevent duplication
     */
    private static final HashMap<String, SimplePlayback> PLAYBACK_CACHE = new HashMap<>();

    /**
     * Creates a new {@link SimplePlayback} object from the given {@link File}.
     *
     * @param file the {@code File} to parse
     * @return the created {@link SimplePlayback} object
     */
    public static SimplePlayback createPlayback(File file) {
        String absPath = null;
        try {
            absPath = file.getCanonicalPath();
        } catch (IOException e) {
            log.warn("Could not find the canonical path of file", e);
        }

        if (absPath != null && PLAYBACK_CACHE.containsKey(absPath)) {
            return PLAYBACK_CACHE.get(absPath);
        } else {
            SimplePlayback output = new SimplePlayback(file);
            PLAYBACK_CACHE.put(absPath, output);

            return output;
        }
    }

    /**
     * The collection of instructions to execute.
     */
    private ArrayList<SimpleInstruction> simpleInstructions;

    /**
     * Creates a {@link SimplePlayback} object from the given {@link File}.
     *
     * @param toParse the {@code File} to parse
     */
    private SimplePlayback(File toParse) {
        parseFile(toParse);
    }

    /**
     * Parses the given {@link File}.
     *
     * @param toParse the {@link File} to parse
     */
    private void parseFile(File toParse) {
        try (BufferedReader in = new BufferedReader(new FileReader(toParse))) {
            // read the header
            String line = in.readLine();
            InstructionFormatter format;
            if (line == null || !line.startsWith("!FORMAT: ")) {
                throw new InvalidFileFormatException("Invalid or missing format declaration");
            } else {
                format = new InstructionFormatter(line.substring(9));
            }

            // great, now start reading the file's true contents
            simpleInstructions = readIndented(in, format, 0, toParse.getPath());
        } catch (IOException e) {
            log.warn("While reading the file, an IOException was thrown", e);
        }
    }

    /**
     * Reads the indented block
     *
     * @param in               the {@link BufferedReader} reading from the {@link File} to produce
     * @param format           the {@link InstructionFormatter} object used to parse information
     * @param indentationLevel the amount of indentation this block is in
     * @param filePath         the path of the file that is being worked with
     * @return an {@link ArrayList} of {@link SimpleInstruction}s to execute
     * @throws IOException if something goes wrong while reading the file
     * @see SimpleInstruction
     */
    private ArrayList<SimpleInstruction> readIndented(BufferedReader in, InstructionFormatter format,
                                                      final int indentationLevel, final String filePath)
            throws IOException {
        ArrayList<SimpleInstruction> output = new ArrayList<>();

        String line;
        while ((line = in.readLine()) != null) {
            String content = line.stripLeading();
            log.trace("content: {}", content);

            if (line.isBlank() || content.startsWith("#")) {
                continue;
            }

            int firstNonSpace = line.length() - content.length();
            if (firstNonSpace % 4 != 0 || firstNonSpace / 4 > indentationLevel) {
                throw new InvalidFileFormatException("Invalid indentation");
            }

            log.trace("Indentation math: compare {} to {}", firstNonSpace / 4, indentationLevel);

            if (firstNonSpace / 4 < indentationLevel) {
                in.reset();

                break;
            }

            // guaranteed: indentationLevel * 4 == firstNonSpace
            if (content.startsWith("INCLUDE ")) {
                Path path = Paths.get(filePath, "..", content.substring(8));
                log.trace("Fragment path: {}", path);
                SimplePlayback inner = SimplePlayback.createPlayback(path.toFile());
                output.addAll(inner.simpleInstructions);
            } else if (content.startsWith("REPEAT ")) {
                int repeat = Integer.parseInt(content.substring(7));
                ArrayList<SimpleInstruction> repeated = readIndented(in, format, indentationLevel + 1, filePath);
                for (int i = 0; i < repeat; i++) {
                    output.addAll(repeated);
                }
            } else {
                output.add(format.parse(content));
            }

            in.mark(1_000);
        }

        return output;
    }

    /**
     * Returns an {@link Iterator} that goes through all of the {@link SimpleInstruction}s in this object.
     *
     * @return an {@link Iterator} that goes through all of the {@link SimpleInstruction}s in this object.
     */
    public Iterator<SimpleInstruction> iterator() {
        return simpleInstructions.iterator();
    }

    /**
     * Executes these instructions as fast as possible.
     *
     * @param r the {@link Robot} object to execute these actions with
     */
    public void executeQuick(Robot r) {
        Iterator<SimpleInstruction> iter = iterator();
        SimpleInstruction prev = null;
        while (iter.hasNext()) {
            SimpleInstruction curr = iter.next();
            curr.execute(r, prev);

            prev = curr;
        }
    }

    /**
     * Executes one set of instructions once every given amount of milliseconds until no simpleInstructions remain.
     *
     * @param r      the {@link Robot} object to execute these actions with
     * @param millis the amount of milliseconds to wait before executing the next set of instructions
     */
    public void execute(Robot r, long millis) {
        Iterator<SimpleInstruction> iter = iterator();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        SimpleInstruction[] prev = new SimpleInstruction[1];
        double[] prevEnd = {System.nanoTime()};
        ses.scheduleAtFixedRate(() -> {
            double start = System.nanoTime(), total, betweenTotal = start - prevEnd[0];
            betweenTotal /= 1_000_000;
            log.trace("Between instructions: {} ms", String.format("%.3f", betweenTotal));

            try {
                if (iter.hasNext()) {
                    SimpleInstruction curr = iter.next();
                    curr.execute(r, prev[0]);

                    prev[0] = curr;
                } else {
                    ses.shutdown();
                    log.info("Shutdown initiated");
                }
            } catch (Exception e) {
                log.error("While executing the instruction set, an exception was thrown.", e);
            }

            total = (prevEnd[0] = System.nanoTime()) - start;
            total /= 1_000_000;
            log.trace("Executing one instruction: {} ms", String.format("%.3f", total));
            log.info("Total for frame: {} ms", String.format("%.3f", total + betweenTotal));
        }, 0, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Compiles this {@link SimplePlayback} into a {@link CompiledPlayback}.
     *
     * @return a {@link CompiledPlayback} that represents this object
     */
    public CompiledPlayback compile() {
        ArrayList<CompiledInstruction> list = new ArrayList<>();
        list.add(simpleInstructions.get(0).compile(null));
        int max = simpleInstructions.size() - 1;
        for (int i = 0; i < max; i++) {
            list.add(simpleInstructions.get(i + 1).compile(simpleInstructions.get(i)));
        }

        return new CompiledPlayback(list);
    }

    /**
     * {@inheritDoc}
     *
     * @return a {@link String} representation of this {@link SimplePlayback} object.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[SimplePlayback simpleInstructions={");
        for (int i = 0; i < simpleInstructions.size(); i++) {
            output.append(simpleInstructions.get(i).toString());
            if (i != simpleInstructions.size() - 1) {
                output.append(", ");
            }
        }
        output.append("}]");

        return output.toString();
    }
}
