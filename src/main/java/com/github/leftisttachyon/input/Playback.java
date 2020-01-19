package com.github.leftisttachyon.input;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
public class Playback {
    /**
     * A cache to store {@link Playback} objects to prevent duplication
     */
    private static final HashMap<String, Playback> PLAYBACK_CACHE = new HashMap<>();

    /**
     * Creates a new {@link Playback} object from the given {@link File}.
     *
     * @param file the {@code File} to parse
     * @return the created {@link Playback} object
     */
    public static Playback createPlayback(File file) {
        String absPath = null;
        try {
            absPath = file.getCanonicalPath();
        } catch (IOException e) {
            log.warn("Could not find the canonical path of file", e);
        }

        if (absPath != null && PLAYBACK_CACHE.containsKey(absPath)) {
            return PLAYBACK_CACHE.get(absPath);
        } else {
            Playback output = new Playback(file);
            PLAYBACK_CACHE.put(absPath, output);

            return output;
        }
    }

    /**
     * The collection of instructions to execute.
     */
    private ArrayList<Instruction> instructions;

    /**
     * Creates a {@link Playback} object from the given {@link File}.
     *
     * @param toParse the {@code File} to parse
     */
    private Playback(File toParse) {
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
            int fileType;
            if (line == null || !line.startsWith("!TYPE: ")) {
                throw new InvalidFileFormatException("Invalid or missing type declaration");
            } else {
                switch (line.substring(7)) {
                    case "DEFAULT":
                        fileType = 0;
                        break;
                    case "FRAGMENT":
                        fileType = 1;
                        break;
                    default:
                        throw new InvalidFileFormatException("Invalid type in type declaration");
                }
            }

            line = in.readLine();
            InstructionFormatter format;
            if (line == null || !line.startsWith("!FORMAT: ")) {
                throw new InvalidFileFormatException("Invalid or missing format declaration");
            } else {
                format = new InstructionFormatter(line.substring(9));
            }

            // great, now start reading the file's true contents
            instructions = readIndented(in, format, 0, toParse.getPath(), fileType);
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
     * @param fileType         the type of file that is being worked with
     * @return an {@link ArrayList} of {@link Instruction}s to execute
     * @throws IOException if something goes wrong while reading the file
     * @see Instruction
     */
    private ArrayList<Instruction> readIndented(BufferedReader in, InstructionFormatter format,
                                                final int indentationLevel, final String filePath, final int fileType)
            throws IOException {
        ArrayList<Instruction> output = new ArrayList<>();

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

            if (firstNonSpace / 4 < indentationLevel) {
                break;
            }

            // guaranteed: indentationLevel * 4 == firstNonSpace
            if (content.startsWith("INCLUDE ")) {
                if (fileType == 0) {
                    Playback inner = Playback.createPlayback(Paths.get(filePath, content.substring(8)).toFile());
                    output.addAll(inner.instructions);
                } else {
                    throw new InvalidFileFormatException("Fragments cannot have include statements");
                }
            } else if (content.startsWith("REPEAT ")) {
                int repeat = Integer.parseInt(content.substring(7));
                ArrayList<Instruction> repeated = readIndented(in, format, indentationLevel + 1, filePath, fileType);
                for (int i = 0; i < repeat; i++) {
                    output.addAll(repeated);
                }
            } else {
                output.add(format.parse(content));
            }
        }

        return output;
    }

    /**
     * Returns an {@link Iterator} that goes through all of the {@link Instruction}s in this object.
     *
     * @return an {@link Iterator} that goes through all of the {@link Instruction}s in this object.
     */
    public Iterator<Instruction> instructionIterator() {
        return instructions.iterator();
    }

    /**
     * Executes these instructions as fast as possible.
     *
     * @param r the {@link Robot} object to execute these actions with
     */
    public void executeQuick(Robot r) {
        Iterator<Instruction> iter = instructionIterator();
        Instruction prev = null;
        while (iter.hasNext()) {
            Instruction curr = iter.next();
            curr.execute(r, prev);

            prev = curr;
        }
    }

    /**
     * Executes one set of instructions once every given amount of milliseconds until no instructions remain.
     *
     * @param r the {@link Robot} object to execute these actions with
     * @param millis the amount of milliseconds to wait before executing the next set of instructions
     */
    public void execute(Robot r, long millis) {
        Iterator<Instruction> iter = instructionIterator();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        Instruction[] prev = new Instruction[1];
        ses.scheduleAtFixedRate(() -> {
            if (iter.hasNext()) {
                Instruction curr = iter.next();
                curr.execute(r, prev[0]);

                prev[0] = curr;
            } else {
                ses.shutdown();
            }
        }, 0, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     *
     * @return a {@link String} representation of this {@link Playback} object.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[Playback instructions={\n");
        for (Instruction i : instructions) {
            output.append(i.toString());
            output.append("\n");
        }
        output.append("}]");

        return output.toString();
    }
}
