package com.github.leftisttachyon.input;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that parses formatted instructions.<br />
 * For keyboard inputs, use the format {@code K&lt;character&gt;}.<br />
 * For mouse inputs, use the format {@code M&lt;button-number&gt;}.
 *
 * @author Jed Wang
 * @since 1.0.0
 */
@Slf4j
public class InstructionFormatter {
    /**
     * The format to use for this InstructionFormatter
     */
    public final Pattern format;
    /**
     * The list of names of the capturing groups
     */
    private final ArrayList<String> names;

    /**
     * Creates a new InstructionFormatter
     *
     * @param format the format to use
     */
    public InstructionFormatter(final String format) {
        names = new ArrayList<>();

        Matcher m = Pattern.compile("<[A-Za-z0-9]+?>").matcher(format);
        StringBuilder regex = new StringBuilder();
        int prev;
        for (prev = 0; m.find(); prev = m.end()) {
            regex.append(format, prev, m.start());
            regex.append("(?");
            regex.append(format, m.start(), m.end());
            regex.append("[\\w-\\.]+?)");

            names.add(format.substring(m.start() + 1, m.end() - 1));
        }
        regex.append(format.substring(prev));
        regex.append("\\s*");

        log.trace("Regex format for \"{}\": \"{}\"", format, regex);
        this.format = Pattern.compile(regex.toString());
    }

    /**
     * Determines whether the given input line matches this object's format.
     *
     * @param line the line to check
     * @return whether the given input line matches this object's format
     */
    public boolean matches(String line) {
        return format.matcher(line).matches();
    }

    /**
     * Parses the given line into an {@link SimpleInstruction}.
     *
     * @param line the line to parse
     * @return an {@link SimpleInstruction} that represents the given line. If the line cannot be parsed, then {@code null}
     * is returned.
     */
    public SimpleInstruction parse(String line) {
        Matcher matcher = format.matcher(line);

        if (!matcher.matches()) {
            return null;
        }

        HashMap<String, String> inputMap = new HashMap<>();
        for (String s : names) {
            inputMap.put(s, matcher.group(s));
        }

        return new SimpleInstruction(inputMap);
    }
}
