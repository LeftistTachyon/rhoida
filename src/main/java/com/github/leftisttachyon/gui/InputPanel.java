package com.github.leftisttachyon.gui;

import com.github.leftisttachyon.input.SimpleInstruction;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * A {@link JPanel} that shows a mock display of inputs.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public class InputPanel extends JPanel {
    /**
     * The height of the text bar
     */
    private static final int TEXT_BAR_HEIGHT = 30;
    /**
     * The height of the text in the text bar
     */
    private static final int TEXT_HEIGHT = 15;
    /**
     * The dimensions of this {@link InputPanel}
     */
    private final int width, height;
    /**
     * The {@link String} to draw
     */
    private String keyString;
    /**
     * The last known position of the mouse.
     */
    private Point mousePos;
    /**
     * The color of the ring
     */
    private Color ringColor;

    /**
     * Creates a new {@link InputPanel}
     *
     * @param size the {@link Dimension} to use as the size of the panel
     */
    public InputPanel(Dimension size) {
        this(size.width, size.height);
    }

    /**
     * Creates a new {@link InputPanel}
     *
     * @param width  the width to set
     * @param height the height to set
     */
    public InputPanel(int width, int height) {
        super();

        if (width < 100) {
            width = 100;
        }
        setPreferredSize(new Dimension(width, height + TEXT_BAR_HEIGHT));

        log.debug("Dimensions: ({}, {} + {})", width, height, TEXT_BAR_HEIGHT);

        this.width = width;
        this.height = height;
    }

    /**
     * A main method to test this panel
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        InputPanel inputPanel = new InputPanel(100, 100);
//        SwingUtilities.invokeLater(() -> {
//            JWindow window = new JWindow();
//            window.add(inputPanel);
//            window.pack();
//
//            window.setOpacity(0.5f);
//            window.setVisible(true);
//
//            SwingUtilities.invokeLater(() -> {
//                HashMap<String, String> temp = new HashMap<>();
//                temp.put("MX", "10");
//                temp.put("MY", "20");
//                temp.put("M1", "e");
//
//                inputPanel.update(new SimpleInstruction(temp));
//
//                try {
//                    Thread.sleep(250);
//                } catch (InterruptedException e) {
//                    log.error("Cannot sleep ;-;");
//                }
//                log.info("Next frame");
//                temp.put("MX", "20");
//
//                inputPanel.update(new SimpleInstruction(temp));
//
//                try {
//                    Thread.sleep(250);
//                } catch (InterruptedException e) {
//                    log.error("Cannot sleep ;-;");
//                }
//                log.info("Next frame");
//                temp.put("MY", "30");
//
//                inputPanel.update(new SimpleInstruction(temp));
//
//                try {
//                    Thread.sleep(250);
//                } catch (InterruptedException e) {
//                    log.error("Cannot sleep ;-;");
//                }
//                log.info("Next frame");
//                temp.put("MX", "30");
//
//                inputPanel.update(new SimpleInstruction(temp));
//            });
//        });

//        InputPanel inputPanel = displayNewInputPanel(0, 0, 10, 10);
//        log.info("inputPanel: {}", inputPanel);
//        log.info("parent: {}", inputPanel.getParent());
//        log.info("grandparent: {}", inputPanel.getParent().getParent());
//        log.info("great grandparent: {}", inputPanel.getParent().getParent().getParent());
//        log.info("window ancestor: {}", SwingUtilities.getWindowAncestor(inputPanel));
    }

    /**
     * Displays a new {@link InputPanel}.
     *
     * @param x      the x-coordinate to show this {@link InputPanel} at
     * @param y      the y-coordinate to show this {@link InputPanel} at
     * @param width  the width to create the {@link InputPanel} with
     * @param height the height to create the {@link InputPanel} with
     * @return the newly created InputPanel
     */
    public static InputPanel displayNewInputPanel(int x, int y, int width, int height) {
        InputPanel inputPanel = new InputPanel(width, height);
        SwingUtilities.invokeLater(() -> {
            JWindow window = new JWindow();
            window.add(inputPanel);
            window.pack();

            window.setOpacity(0.5f);
            window.setLocation(x, y);
            window.setVisible(true);
        });

        return inputPanel;
    }

    @Override
    public void paint(Graphics g) {
        log.debug("Painted");

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2D.setColor(Color.WHITE);
        g2D.fillRect(0, height, width, TEXT_BAR_HEIGHT);

        if (keyString != null) {
            g2D.setColor(Color.BLACK);
            g2D.setFont(new Font("Consolas", Font.PLAIN, TEXT_HEIGHT));
            g2D.drawString(keyString, 5, height + TEXT_HEIGHT + 5);
        }

        g2D.setColor(new Color(80, 80, 80));
        g2D.fillRect(0, 0, width, height);

        log.trace("ringColor: {}", ringColor);

        // draw ring, if any
        if (mousePos != null) {
            log.trace("Drawing mouse pointer @ ({}, {})", mousePos.x, mousePos.y);

            if (ringColor != null) {
                log.trace("Oooo, a ring!");
                g2D.setColor(Color.WHITE);
                g2D.fillOval(mousePos.x - 10, mousePos.y - 10, 20, 20);

                g2D.setColor(ringColor);
                g2D.fillOval(mousePos.x - 8, mousePos.y - 8, 16, 16);
            }

            g2D.setColor(Color.WHITE);
            g2D.fillOval(mousePos.x - 5, mousePos.y - 5, 10, 10);
        }
    }

    /**
     * Updates the view of this {@link InputPanel} with the information in the given {@link SimpleInstruction}.
     *
     * @param instruction the information to update this {@link InputPanel} with
     */
    public void update(SimpleInstruction instruction) {
        HashMap<String, String> inputMap = instruction.getInputMap();
        log.trace("inputMap:");
        if (log.isTraceEnabled()) {
            for (Map.Entry<String, String> entry : inputMap.entrySet()) {
                log.trace("{} = {}", entry.getKey(), entry.getValue());
            }
        }
        TreeSet<String> keys = new TreeSet<>(inputMap.keySet());
        StringBuilder temp = new StringBuilder();
        int mouse = 0, x = -1, y = -1;

        for (String input : keys) {
            String val = inputMap.get(input);
            if (input.equals("MX")) {
                if (SimpleInstruction.isInput(val)) {
                    x = Integer.parseInt(val);
                }
            } else if (input.equals("MY")) {
                if (SimpleInstruction.isInput(val)) {
                    y = Integer.parseInt(val);
                }
            } else if (input.startsWith("K")) {
                if (!SimpleInstruction.isInput(val)) {
                    temp.append(input.substring(1));
                } else {
                    temp.append(val);
                }
                temp.append(' ');
            } else if (input.startsWith("M")) {
                if (SimpleInstruction.isInput(val)) {
                    switch (input.substring(1)) {
                        case "1":
                            mouse |= 0xFF << 6;
                            break;
                        case "2":
                            mouse |= 0xFF << 3;
                            break;
                        case "3":
                            mouse |= 0xFF;
                            break;
                    }
                }
            }
        }

        keyString = temp.length() > 0 ? temp.substring(0, temp.length() - 1) : null;
        log.trace("({}, {})", x, y);
        if (x != -1 && y != -1) {
            mousePos = new Point(x, y);
        }

        if (mouse != 0) {
            ringColor = new Color(mouse);
        } else {
            ringColor = null;
        }

        repaint();
    }
}
