package com.github.leftisttachyon.gui;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A {@link Component} that represents an opened playback file.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
@Getter
public class FileTab extends JScrollPane {
    /**
     * The inner {@link JPanel} inside of this {@link FileTab}
     */
    private final JPanel innerPanel;
    /**
     * The {@link JTextArea} in this {@link FileTab}
     */
    private final JTextArea textArea;
    /**
     * The {@link TextLineNumber} object used for line numbers on the left
     */
    private final TextLineNumber lineNumbers;
    /**
     * The {@link TextLineNumber} object used for frame numbers on the right
     */
    private final TextLineNumber frameNumbers;
    /**
     * The file that this file tab is saving to
     */
    @Setter
    private File file;
    /**
     * Stores whether the content of the {@link JTextArea} has been changed since the last save.
     */
    private boolean changed = false;

    /**
     * Creates a new {@link FileTab}.
     */
    public FileTab() {
        innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());

        textArea = new JTextArea(5, 20);
        textArea.setTabSize(4);
        textArea.setLineWrap(true);
        textArea.setText("!FORMAT: \n");
        innerPanel.add(textArea);

        lineNumbers = new TextLineNumber(textArea);
        textArea.getDocument().addDocumentListener(lineNumbers);

        textArea.addCaretListener(lineNumbers);
        textArea.addPropertyChangeListener("font", lineNumbers);
        innerPanel.add(lineNumbers, BorderLayout.WEST);

        frameNumbers = new TextLineNumber(textArea, 4, TextLineNumber.LEFT_ALIGNMENT);
        textArea.getDocument().addDocumentListener(frameNumbers);
        textArea.addCaretListener(frameNumbers);
        textArea.addPropertyChangeListener("font", frameNumbers);

        innerPanel.add(frameNumbers, BorderLayout.EAST);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        setViewportView(innerPanel);
        getVerticalScrollBar().setUnitIncrement(16);
    }

    /**
     * Creates a new {@link FileTab} and opens up the given {@link File}
     *
     * @param toOpen the {@link File} to open
     */
    public FileTab(File toOpen) {
        this();

        file = toOpen;
        open();
    }

    /**
     * Toggles the visibility of line numbers.
     */
    public void toggleLineNums() {
        lineNumbers.setVisible(!lineNumbers.isVisible());

        revalidate();
    }

    /**
     * Toggles the visibility of frame numbers.
     */
    public void toggleFrameNums() {
        frameNumbers.setVisible(!frameNumbers.isVisible());

        revalidate();
    }

    /**
     * Attempts to save the contents of the {@link JTextArea} to the internal file.
     *
     * @return whether saving was successful
     */
    public boolean save() {
        if (file == null) {
            return false;
        }
        try (BufferedWriter out = Files.newBufferedWriter(file.toPath())) {
            String text = textArea.getText();
            out.write(text);
            changed = false;
            return true;
        } catch (IOException e) {
            log.warn("An IOException was thrown while attempting to save", e);
            return false;
        }
    }

    /**
     * Returns the name of the internal file, if any exists. If none exists, then {@code null} is returned.
     *
     * @return the name of the internal file
     */
    public String getFileName() {
        return file == null ? null : file.getName();
    }

    /**
     * Inserts the given {@link String} at the location of the cursor
     *
     * @param s the {@link String} to insert
     */
    public void insertAtCursor(String s) {
        int caretPosition = textArea.getCaretPosition();
        textArea.insert(s, caretPosition);
    }

    /**
     * Opens the internally stored file
     */
    private void open() {
        try (BufferedReader in = Files.newBufferedReader(file.toPath())) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            textArea.setText(sb.toString());
            changed = false;
        } catch (IOException e) {
            log.warn("An IOException was thrown while attempting to open a file", e);
            JOptionPane.showMessageDialog(innerPanel, "The selected file could not be opened", "Error upon opening",
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Cannot open file");
        }
    }

    /**
     * Called when the content of the {@link JTextArea} changes.
     */
    private void changed() {
        changed = true;
    }
}
