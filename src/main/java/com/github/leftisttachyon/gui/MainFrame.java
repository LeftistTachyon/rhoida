package com.github.leftisttachyon.gui;

import com.github.leftisttachyon.input.InvalidFileFormatException;
import com.github.leftisttachyon.input.SimpleInstruction;
import com.github.leftisttachyon.input.SimplePlayback;
import com.github.leftisttachyon.input.compiled.CompiledPlayback;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.awt.event.KeyEvent.*;

/**
 * The main GUI window
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public final class MainFrame extends JFrame {

    /**
     * The list of tabs
     */
    private final ArrayList<FileTab> tabs;
    /**
     * The {@link JFileChooser} instance used in this {@link MainFrame}.
     */
    private final JFileChooser fc;
    /**
     * A {@link JTabbedPane} that contains all of the goods
     */
    private JTabbedPane fileTabbedPane;
    /**
     * A formatted text field that handles the x-offset
     */
    private JFormattedTextField xOffsetField;
    /**
     * A formatted text field that handles the y-offset
     */
    private JFormattedTextField yOffsetField;
    /**
     * A formatted text field that handles the delay between each frame.
     */
    private JFormattedTextField frameDelayField;
    /**
     * The currently running thread, if any
     */
    private Thread running;
    /**
     * A counter for unnamed files
     */
    private int cnt = 2;

    /**
     * Creates a new MainFrame
     */
    public MainFrame() {
        tabs = new ArrayList<>();
        fc = new JFileChooser();

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        integerFormat.setGroupingUsed(false);
        integerFormat.setMinimumIntegerDigits(1);

        JLabel xOffsetLabel = new JLabel(), yOffsetLabel = new JLabel(),
                frameDelayLabel = new JLabel(), msLabel = new JLabel();
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(), editMenu = new JMenu(), viewMenu = new JMenu(), runMenu = new JMenu();
        JMenuItem newMenuItem = new JMenuItem(), openMenuItem = new JMenuItem(), saveMenuItem = new JMenuItem(),
                saveAsMenuItem = new JMenuItem(), saveAllMenuItem = new JMenuItem(), exitMenuItem = new JMenuItem(),
                includeFileMenuItem = new JMenuItem(), closeTabMenuItem = new JMenuItem(),
                closeAllTabsMenuItem = new JMenuItem(), circleTestMenuItem = new JMenuItem(),
                runMenuItem = new JMenuItem();
        JCheckBoxMenuItem lineNumCheckBox = new JCheckBoxMenuItem(), frameNumCheckBox = new JCheckBoxMenuItem();
        JButton chooseWindowButton = new JButton("Choose Window...");

        fileTabbedPane = new JTabbedPane();
        xOffsetField = new JFormattedTextField(integerFormat);
        yOffsetField = new JFormattedTextField(integerFormat);
        frameDelayField = new JFormattedTextField(integerFormat);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("RhoIda");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeAllTabs(null);
            }
        });

        Font segoe12 = new Font("Segoe UI", Font.PLAIN, 12); // NOI18N
        xOffsetLabel.setFont(segoe12);
        xOffsetLabel.setText("X offset:");

        xOffsetField.setFont(segoe12);
        xOffsetField.setText("0");

        fileTabbedPane.setFont(new Font("Monospace", Font.PLAIN, 11)); // NOI18N
        fileTabbedPane.addTab("unsaved1", newFileTab());

        yOffsetLabel.setFont(segoe12);
        yOffsetLabel.setText("Y offset:");

        yOffsetField.setFont(segoe12);
        yOffsetField.setText("0");

        frameDelayField.setFont(segoe12);
        frameDelayField.setText("16");

        frameDelayLabel.setFont(segoe12);
        frameDelayLabel.setText("Frame delay:");

        msLabel.setFont(segoe12);
        msLabel.setText("ms");

        chooseWindowButton.setFont(segoe12);
        chooseWindowButton.addActionListener(this::openWindowSelect);

        fileMenu.setText("File");
        fileMenu.setMnemonic(VK_F);
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu");

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        newMenuItem.setText("New");
        newMenuItem.addActionListener(this::create);
        newMenuItem.setMnemonic(VK_N);
        newMenuItem.getAccessibleContext().setAccessibleDescription("Creates a new tab.");
        fileMenu.add(newMenuItem);

        openMenuItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openMenuItem.setText("Open...");
        openMenuItem.addActionListener(this::open);
        openMenuItem.setMnemonic(VK_O);
        openMenuItem.getAccessibleContext().setAccessibleDescription("Opens a file for editing.");
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(this::save);
        saveMenuItem.setMnemonic(VK_S);
        saveMenuItem.getAccessibleContext().setAccessibleDescription("Saves the currently opened file.");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(this::saveAs);
        saveMenuItem.setMnemonic(VK_A);
        saveMenuItem.getAccessibleContext().setAccessibleDescription("Saves the currently opened file as a new file.");
        fileMenu.add(saveAsMenuItem);

        saveAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        saveAllMenuItem.setText("Save All");
        saveAllMenuItem.addActionListener(this::saveAll);
        saveAllMenuItem.setMnemonic(VK_V);
        saveAllMenuItem.getAccessibleContext().setAccessibleDescription("Saves all opened files.");
        fileMenu.add(saveAllMenuItem);
        fileMenu.add(new JPopupMenu.Separator());

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(this::exit);
        exitMenuItem.setMnemonic(VK_E);
        exitMenuItem.getAccessibleContext().setAccessibleDescription("Exits this program");
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setMnemonic(VK_E);
        editMenu.getAccessibleContext().setAccessibleDescription("Edit menu");

        includeFileMenuItem.setAccelerator(KeyStroke.getKeyStroke("control I"));
        includeFileMenuItem.setText("Include File...");
        includeFileMenuItem.addActionListener(this::insertInclude);
        includeFileMenuItem.setMnemonic(VK_I);
        includeFileMenuItem.getAccessibleContext().setAccessibleDescription("Inserts an INCLUDE statement with the selected file");
        editMenu.add(includeFileMenuItem);

        menuBar.add(editMenu);

        viewMenu.setText("View");
        viewMenu.setMnemonic(VK_V);
        viewMenu.getAccessibleContext().setAccessibleDescription("The menu that controls the looks of this window");

        JMenuItem nextTabMenuItem = new JMenuItem();
        nextTabMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt RIGHT"));
        nextTabMenuItem.setText("Next Tab");
        nextTabMenuItem.addActionListener(this::nextTab);
        nextTabMenuItem.setMnemonic(VK_N);
        nextTabMenuItem.getAccessibleContext().setAccessibleDescription("Moves to the next tab to the right");
        viewMenu.add(nextTabMenuItem);

        JMenuItem previousTabMenuItem = new JMenuItem();
        previousTabMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt LEFT"));
        previousTabMenuItem.setText("Previous Tab");
        previousTabMenuItem.addActionListener(this::previousTab);
        previousTabMenuItem.setMnemonic(VK_P);
        previousTabMenuItem.getAccessibleContext().setAccessibleDescription("Moves to the next tab to the left");
        viewMenu.add(previousTabMenuItem);
        viewMenu.add(new JPopupMenu.Separator());

        closeTabMenuItem.setAccelerator(KeyStroke.getKeyStroke("control W"));
        closeTabMenuItem.setText("Close Tab");
        closeTabMenuItem.addActionListener(this::closeTab);
        closeTabMenuItem.setMnemonic(VK_C);
        closeTabMenuItem.getAccessibleContext().setAccessibleDescription("Closes the current tab");
        viewMenu.add(closeTabMenuItem);

        closeAllTabsMenuItem.setAccelerator(KeyStroke.getKeyStroke("control shift W"));
        closeAllTabsMenuItem.setText("Close All Tabs");
        closeAllTabsMenuItem.addActionListener(this::closeAllTabs);
        closeAllTabsMenuItem.setMnemonic(VK_A);
        closeAllTabsMenuItem.getAccessibleContext().setAccessibleDescription("Closes all opened tabs");
        viewMenu.add(closeAllTabsMenuItem);
        viewMenu.add(new JPopupMenu.Separator());

        lineNumCheckBox.setSelected(true);
        lineNumCheckBox.setText("Enable Line Numbers");
        lineNumCheckBox.addActionListener(this::toggleLineNums);
        lineNumCheckBox.setMnemonic(VK_L);
        lineNumCheckBox.getAccessibleContext().setAccessibleDescription(
                "If checked, shows line numbers on the left of the text area");
        viewMenu.add(lineNumCheckBox);

        frameNumCheckBox.setSelected(true);
        frameNumCheckBox.setText("Enable Frame Numbers");
        frameNumCheckBox.addActionListener(this::toggleFrameNums);
        frameNumCheckBox.setMnemonic(VK_F);
        frameNumCheckBox.getAccessibleContext().setAccessibleDescription(
                "If checked, shows frame numbers on the right of the text area");
        viewMenu.add(frameNumCheckBox);

        menuBar.add(viewMenu);

        runMenu.setText("Run");
        runMenu.setMnemonic(VK_R);
        runMenu.getAccessibleContext().setAccessibleDescription("A menu that controls running the open program");

        circleTestMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        circleTestMenuItem.setText("Circle Test");
        circleTestMenuItem.addActionListener(this::runCircleTest);
        circleTestMenuItem.setMnemonic(VK_C);
        circleTestMenuItem.getAccessibleContext().setAccessibleDescription(
                "Shows a running visual of the open program");
        runMenu.add(circleTestMenuItem);

        runMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
        runMenuItem.setText("Run");
        runMenuItem.addActionListener(this::run);
        runMenuItem.setMnemonic(VK_R);
        runMenuItem.getAccessibleContext().setAccessibleDescription("Runs the opened program");
        runMenu.add(runMenuItem);

        runMenu.add(new JPopupMenu.Separator());

        JMenuItem stopMenuItem = new JMenuItem("Stop");
        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke("control ESC"));
        stopMenuItem.addActionListener(this::stop);
        stopMenuItem.setMnemonic(VK_S);
        stopMenuItem.getAccessibleContext().setAccessibleDescription("Stops the currently running thread");
        runMenu.add(stopMenuItem);

        menuBar.add(runMenu);

        JMenu otherMenu = new JMenu("Other");
        otherMenu.setMnemonic(VK_O);
        otherMenu.getAccessibleContext().setAccessibleDescription("A menu with other stuff in it");

        JMenuItem referenceMenuItem = new JMenuItem("User reference");
        referenceMenuItem.addActionListener((evt) -> JOptionPane.showMessageDialog(this, new JLabel(
                        "<html><body style='text-align:center'>" +
                                "<h2><b>User Reference</b></h2>" +
                                "<h3><i>Heading</i></h3>" +
                                "<p>The first line in each file must be a format declaration. A format declaration is in the format<br>" +
                                "<code>!FORMAT: &lt;...&gt;...</code> with the <code>...</code> inside the angle brackets<br>" +
                                "representing a input specifier. You can add as many as you would like.</p>" +
                                "<h3><i>Input Specifiers</i></h3>" +
                                "Input specifiers direct RhoIda to which keys and mouse controls should be inputted.<br>" +
                                "Below is a table containing all input specifiers.<br>" +
                                "<table><tr><td><b>Input Specifier</b></td><td><b>Corresponding Control</b></td></tr>" +
                                "<tr><td>MX</td><td>Mouse X Position</td></tr>" +
                                "<tr><td>MY</td><td>Mouse Y Position</td></tr>" +
                                "<tr><td>K<code>KEY</code></td><td><code>KEY</code></td></tr>" +
                                "<tr><td>M<code>NUM</code></td><td>Mouse button number <code>NUM</code></td></tr></table>" +
                                "<h3><i>Input Directions</i></h3>" +
                                "<p>For input directions, follow your input format. For example, a format of <code>&lt;KA&gt; &lt;KB&gt;</code><br>" +
                                "will match with <code>A B</code> and fail with <code>AB</code>. For mouse x- and y-coordinates,<br>" +
                                "only numbers will be accepted as input.</p><br>" +
                                "<p><code>_</code>, <code>-</code>, and <code>.</code> represent no input. All other characters represent input.<br>" +
                                "For example, <code>. .</code> for the above sample format will yield no input on that frame.</p>" +
                                "<h3><i>Other statements</i></h3>" +
                                "<p>To repeat a block of commands, place a <code>REPEAT #</code> statement at the top of the block<br>" +
                                "(with the # representing the number of times to repeat the below statements, which should be indented<br>" +
                                "by 4 spaces each. You can nest <code>REPEAT</code> statements, but make sure you indent the<br>" +
                                "corresponding statements.</p><br>" +
                                "<p>Comments, which are lines starting with <code>#</code>, are not read by the interpreter.<p><br>" +
                                "<p>You can include other files by using an <code>INCLUDE</code> statement. Insert the <i>relative</i><br>" +
                                "file path of the file to be included after the <code>INCLUDE</code> statement to make the interpreter<br>" +
                                "insert the contents of that file in that position in the file while running.</p>" +
                                "</body></html>", SwingConstants.CENTER),
                "User reference", JOptionPane.PLAIN_MESSAGE));
        referenceMenuItem.setMnemonic(VK_R);
        referenceMenuItem.getAccessibleContext().setAccessibleDescription("Opens the user reference");
        otherMenu.add(referenceMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((evt) -> JOptionPane.showMessageDialog(this, new JLabel(
                        "<html><body style='text-align:center'>" +
                                "<h2><b>About</b></h2>" +
                                "<p>Developers:<br>LeftistTachyon</p><br>" +
                                "<p>Mental support:<br>Imaproshaman</p>" +
                                "</body></html>", SwingConstants.CENTER),
                "About", JOptionPane.PLAIN_MESSAGE));
        aboutMenuItem.setMnemonic(VK_A);
        aboutMenuItem.getAccessibleContext().setAccessibleDescription("Opens the about screen");
        otherMenu.add(aboutMenuItem);

        menuBar.add(otherMenu);

        setJMenuBar(menuBar);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(xOffsetLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(xOffsetField, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(yOffsetLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yOffsetField, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(chooseWindowButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 154, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(frameDelayLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(frameDelayField, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(msLabel))
                                .addComponent(fileTabbedPane))
                        .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(xOffsetLabel)
                                .addComponent(xOffsetField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(yOffsetLabel)
                                .addComponent(yOffsetField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(chooseWindowButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(5)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(frameDelayLabel)
                                .addComponent(frameDelayField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(msLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTabbedPane, GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                        .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void openWindowSelect(ActionEvent evt) {
        List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
        HashMap<String, Rectangle> map = new HashMap<>();
        for (DesktopWindow window : windows) {
            String title = window.getTitle();
            if (!title.isEmpty()) {
                map.put(title, window.getLocAndSize());
            }
        }

        Object[] options = map.keySet().toArray();
        Object o = JOptionPane.showInputDialog(this, "Select the window to focus on", "Window Selection",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (o == null) return;

        Rectangle rect = map.get(o.toString());
        xOffsetField.setValue(rect.x);
        yOffsetField.setValue(rect.y);
    }

    private void previousTab(ActionEvent evt) {
        int idx = fileTabbedPane.getSelectedIndex() - 1;
        if (idx == -1) {
            idx += fileTabbedPane.getTabCount();
        }

        fileTabbedPane.setSelectedIndex(idx);
    }

    private void nextTab(ActionEvent evt) {
        int idx = fileTabbedPane.getSelectedIndex() + 1;
        if (idx == fileTabbedPane.getTabCount()) {
            idx = 0;
        }

        fileTabbedPane.setSelectedIndex(idx);
    }

    private void open(ActionEvent evt) {
        int returnVal = fc.showOpenDialog(this);
        log.trace("Open dialog returned {}", returnVal);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            int alreadyOpened = -1;
            for (int i = 0; i < tabs.size(); i++) {
                if (file.getName().equals(tabs.get(i).getFileName())) {
                    alreadyOpened = i;
                    break;
                }
            }

            if (alreadyOpened != -1) {
                fileTabbedPane.setSelectedIndex(alreadyOpened);
            } else {
                FileTab fileTab = newFileTab(file);
                if (fileTab != null) {
                    fileTabbedPane.addTab(file.getName(), fileTab);
                    fileTabbedPane.setSelectedIndex(fileTabbedPane.getTabCount() - 1);
                }
            }
        }
    }

    private void save(ActionEvent evt) {
        FileTab selectedTab = getSelectedTab();
        if (selectedTab.getFile() == null) {
            saveAs(evt);
        } else {
            if (!selectedTab.save()) {
                JOptionPane.showMessageDialog(this, "This file could not be saved", "Save failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void create(ActionEvent evt) {
        fileTabbedPane.addTab("unsaved" + (cnt++), newFileTab());
        fileTabbedPane.setSelectedIndex(fileTabbedPane.getTabCount() - 1);
    }

    private void toggleLineNums(ActionEvent evt) {
        getSelectedTab().toggleLineNums();
    }

    private void saveAs(ActionEvent evt) {
        int returnVal = fc.showSaveDialog(this);
        log.trace("Save dialog returned {}", returnVal);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            FileTab selectedTab = getSelectedTab();
            selectedTab.setFile(file);
            fileTabbedPane.setTitleAt(fileTabbedPane.getSelectedIndex(),
                    selectedTab.getFileName());
            if (!selectedTab.save()) {
                JOptionPane.showMessageDialog(this, "This file could not be saved", "Save failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAll(ActionEvent evt) {
        for (int i = 0; i < tabs.size(); i++) {
            FileTab tab = tabs.get(i);
            if (tab.getFile() == null) {
                fileTabbedPane.setSelectedIndex(i);
                saveAs(evt);
            } else {
                if (!tab.save()) {
                    JOptionPane.showMessageDialog(this, tab.getFileName() + " could not be saved", "Save failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exit(ActionEvent evt) {
        closeAllTabs(evt);
        dispose();
        System.exit(0);
    }

    private void insertInclude(ActionEvent evt) {
        save(evt);

        int returnVal = fc.showOpenDialog(this);
        log.trace("Open dialog returned {}", returnVal);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            FileTab fileTab = newFileTab(file);
            if (fileTab != null) {
                FileTab selectedTab = getSelectedTab();
                if (selectedTab.getFile().equals(file)) {
                    JOptionPane.showMessageDialog(this, "Cannot include self", "Invalid inclusion",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    boolean alreadyOpened = false;
                    for (FileTab tab : tabs) {
                        if (tab.getFile() != null &&
                                tab.getFileName().equals(file.getName())) {
                            alreadyOpened = true;
                            break;
                        }
                    }
                    if (!alreadyOpened) {
                        fileTabbedPane.addTab(file.getName(), fileTab);
                    }
                    String relLocation = selectedTab.getFile().getParentFile().toURI()
                            .relativize(file.toURI()).getPath();
                    selectedTab.insertAtCursor("INCLUDE " + relLocation + '\n');
                }
            }
        }
    }

    private void toggleFrameNums(ActionEvent evt) {
        getSelectedTab().toggleFrameNums();
    }

    private void runCircleTest(ActionEvent evt) {
        save(evt);

        File f = getSelectedTab().getFile();
        if (f == null) {
            return;
        }

        int x = Integer.parseInt(xOffsetField.getText()),
                y = Integer.parseInt(yOffsetField.getText());
//        SimpleInstruction.setX_OFFSET(x);
//        SimpleInstruction.setY_OFFSET(y);

        SimplePlayback playback = SimplePlayback.createPlayback(f);
        int maxX = -1, maxY = -1;
        for (SimpleInstruction ins : playback) {
            HashMap<String, String> inputMap = ins.getInputMap();
            String valX = inputMap.get("MX");
            if (valX != null && valX.matches("\\d+")) {
                int tempX = Integer.parseInt(valX);
                if (tempX > maxX) {
                    maxX = tempX;
                }
            }

            String valY = inputMap.get("MY");
            if (valY != null && valY.matches("\\d+")) {
                int tempY = Integer.parseInt(valY);
                if (tempY > maxY) {
                    maxY = tempY;
                }
            }
        }

        Iterator<SimpleInstruction> iter = playback.iterator();
        InputPanel inputPanel = InputPanel.displayNewInputPanel(x, y, maxX + 10, maxY + 10);

        int frameDelay = Integer.parseInt(frameDelayField.getText());
        running = new Thread(() -> {
            while (iter.hasNext()) {
                SimpleInstruction ins = iter.next();
                log.trace("executing instruction: {}", ins);
                inputPanel.update(ins);
                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException e) {
                    log.warn("The sleep instruction was interrupted, continuing");
                }
            }
            SwingUtilities.getWindowAncestor(inputPanel).dispose();
        }) {
            @Override
            public void interrupt() {
                super.interrupt();

                SwingUtilities.getWindowAncestor(inputPanel).dispose();
            }
        };
        running.start();
    }

    private void run(ActionEvent evt) {
        save(evt);

        File file = getSelectedTab().getFile();
        if (file == null) {
            return;
        }

        int x = Integer.parseInt(xOffsetField.getText()),
                y = Integer.parseInt(yOffsetField.getText());
        SimpleInstruction.setX_OFFSET(x);
        SimpleInstruction.setY_OFFSET(y);

        SimplePlayback uncompiled;
        CompiledPlayback compiled;
        try {
            SimplePlayback.clearCache();
            uncompiled = SimplePlayback.createPlayback(file);
            compiled = uncompiled.compile();
        } catch (InvalidFileFormatException ife) {
            JOptionPane.showMessageDialog(this, "The format of the file was incorrect:\n" + ife.getMessage(),
                    "Badly formatted file", JOptionPane.WARNING_MESSAGE);
            log.info("InvalidFileFormatException was thrown", ife);
            return;
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please check the data of the file:\n" + nfe.getMessage(),
                    "Bad file data", JOptionPane.WARNING_MESSAGE);
            log.info("NumberFormatException was thrown", nfe);
            return;
        }

        int frameDelay = Integer.parseInt(frameDelayField.getText());
        try {
            running = compiled.execute(new Robot(), frameDelay);
        } catch (AWTException e) {
            log.warn("An exception was thrown while creating a Robot", e);
        }
    }

    private void closeTab(ActionEvent evt) {
        int temp = fileTabbedPane.getSelectedIndex();
        FileTab selectedTab = getSelectedTab();
        log.trace("file: {}", selectedTab.getFile());
        log.trace("isChanged: {}", selectedTab.isChanged());
        if (selectedTab.getFile() == null || selectedTab.isChanged()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure that you want to close this without saving?",
                    "Closing confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == 1) {
                return;
            }
        }
        fileTabbedPane.remove(temp);
        tabs.remove(temp);
        if (tabs.size() != fileTabbedPane.getTabCount()) {
            log.warn("Sizes not equal: {} vs {}", tabs, fileTabbedPane.getTabCount());
        }
        fileTabbedPane.setSelectedIndex(Math.min(temp, fileTabbedPane.getTabCount() - 1));

        if (fileTabbedPane.getTabCount() == 0) {
            create(evt);
        }
    }

    private void closeAllTabs(ActionEvent evt) {
        int tabCount = fileTabbedPane.getTabCount();
        log.debug("Tab count: {}", tabCount);
        for (int i = 0; i < tabCount; i++) {
            fileTabbedPane.remove(0);
        }
        tabs.clear();

        create(evt);
    }

    private void stop(ActionEvent evt) {
        log.info("running: {}", running);
        if (running != null) running.interrupt();
    }

    private FileTab newFileTab() {
        FileTab tab = new FileTab();
        tabs.add(tab);
        fileTabbedPane.addChangeListener(tab.getChangeListener());
        return tab;
    }

    private FileTab newFileTab(File f) {
        FileTab tab;
        try {
            tab = new FileTab(f);
        } catch (RuntimeException re) {
            return null;
        }
        tabs.add(tab);
        fileTabbedPane.addChangeListener(tab.getChangeListener());
        return tab;
    }

    private FileTab getSelectedTab() {
        int idx = fileTabbedPane.getSelectedIndex();
        return tabs.get(idx);
    }
}
