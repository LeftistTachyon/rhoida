package com.github.leftisttachyon;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import org.junit.jupiter.api.Test;

public class WindowFinder {
    @Test
    public void listWindowLocations() {
        for(DesktopWindow window: WindowUtils.getAllWindows(true)) {
            System.out.println(window.getTitle() + ": " + window.getLocAndSize());
        }
    }
}
