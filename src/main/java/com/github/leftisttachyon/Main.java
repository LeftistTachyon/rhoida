package com.github.leftisttachyon;

import com.github.leftisttachyon.input.SimpleInstruction;
import com.github.leftisttachyon.input.SimplePlayback;
import com.github.leftisttachyon.input.compiled.CompiledPlayback;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * The main class
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public class Main {
    /**
     * The main method
     *
     * @param args the command line arguments
     * @throws IOException if something goes wrong
     * @throws AWTException if the {@link Robot} could not be created successfully
     */
    public static void main(String[] args) throws IOException, AWTException {
        SimpleInstruction.setOffset(new Point(8, 24));

        File file = new File("test.txt");
        log.trace("File canonical path: {}", file.getCanonicalPath());

        SimplePlayback simplePlayback = SimplePlayback.createPlayback(file);
        log.trace("SimplePlayback object: {}", simplePlayback);

        Robot r = new Robot();

        // simplePlayback.execute(r, 16);

        CompiledPlayback compiledPlayback = simplePlayback.compile();
        log.trace("CompiledPlayback object: {}", compiledPlayback);

        compiledPlayback.execute(r, 16);

//        simplePlayback.executeQuick(r);

        // testing space lmao
        // aaaaaabb
        //
    }
}
