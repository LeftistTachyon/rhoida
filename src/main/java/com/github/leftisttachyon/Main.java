package com.github.leftisttachyon;

import com.github.leftisttachyon.input.Playback;
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
        File file = new File("test.txt");
        log.trace("File canonical path: {}", file.getCanonicalPath());

        Playback playback = Playback.createPlayback(file);
        log.trace("Playback object: {}", playback);

        Robot r = new Robot();

        playback.execute(r, 16);
//        playback.executeQuick(r);

        // testing space lmao
        // aaaaaa
        // aaaaaabb
    }
}
