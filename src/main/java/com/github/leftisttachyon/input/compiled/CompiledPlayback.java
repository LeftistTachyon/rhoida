package com.github.leftisttachyon.input.compiled;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class that represents a compiled {@link com.github.leftisttachyon.input.SimplePlayback}
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
@Data
@Getter(AccessLevel.NONE)
public class CompiledPlayback implements Iterable<CompiledInstruction> {
    /**
     * An {@link ArrayList} of {@link CompiledInstruction}s
     */
    private final ArrayList<CompiledInstruction> instructions;

    /**
     * Executes all of the instructions contained in this {@link CompiledPlayback} as quickly as possible.
     *
     * @param r the {@link Robot} to execute these instructions with
     */
    public void executeQuick(Robot r) {
        for (CompiledInstruction ins : instructions) {
            ins.execute(r);
        }
    }

    /**
     * Executes one instruction once every the given number of milliseconds.
     *
     * @param r      the {@link Robot} to execute these instructions with
     * @param millis the number of milliseconds to wait before executing the next set of instructions
     */
    public void execute(Robot r, long millis) {
        Iterator<CompiledInstruction> iter = instructions.iterator();

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            double start = System.nanoTime();

            if (iter.hasNext()) {
                CompiledInstruction curr = iter.next();
                log.trace("Executing {} ...", curr);
                curr.execute(r);
            } else {
                ses.shutdown();
                log.info("Shutdown initiated");
            }

            double total = System.nanoTime() - start;
            total /= 1000000;
            log.info("Time needed: {} ms", String.format("%.3f", total));
        }, 0, millis, TimeUnit.MILLISECONDS);

        log.info("Execution started");
    }

    /**
     * Returns an {@link Iterator} that goes through all of the {@link CompiledInstruction}s in this playback
     *
     * @return an {@link Iterator} that goes through all of the {@link CompiledInstruction}s in this playback
     */
    public Iterator<CompiledInstruction> iterator() {
        return instructions.iterator();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[CompiledPlayback instructions={");
        for (int i = 0; i < instructions.size(); i++) {
            output.append(instructions.get(i).toString());
            if (i != instructions.size() - 1) {
                output.append(", ");
            }
        }
        output.append("}]");

        return output.toString();
    }
}
