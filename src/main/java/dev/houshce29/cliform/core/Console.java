package dev.houshce29.cliform.core;

import dev.houshce29.cliform.util.VisibleForTesting;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Console object. Does simple displaying, prompting, clearing, etc.
 */
public class Console implements Closeable {
    private static final String[] CLEAR_COMMAND = resolveClearCommand();
    private final PrintStream out;
    private final Scanner in;
    private final java.io.Console systemConsole;


    public Console() {
        this(System.out, System.in, System.console());
    }

    @VisibleForTesting
    Console(PrintStream out, InputStream in, java.io.Console systemConsole) {
        this.out = out;
        this.in = new Scanner(in);
        this.systemConsole = systemConsole;
    }

    /**
     * Clears the console.
     * @return This console object for chaining.
     */
    public Console clear() {
        invokeClear();
        return this;
    }

    /**
     * Writes out to the console, on a single line.
     * @return This console object for chaining.
     */
    public Console write(Object object) {
        out.print(object);
        return this;
    }

    /**
     * Writes out a new line.
     * @return This console object for chaining.
     */
    public Console newLine() {
        out.println();
        return this;
    }

    /**
     * Writes out the object and adds a new line after.
     * @return This console object for chaining.
     */
    public Console writeLine(Object line) {
        out.println(line);
        return this;
    }

    /**
     * Writes each item into its own line.
     * @return This console object for chaining.
     */
    public Console writeLines(Object... lines) {
        for (Object line : lines) {
            out.println(line);
        }
        return this;
    }

    /**
     * Prints the message and awaits user input.
     * @param message Message to prompt for input.
     * @return The input string.
     */
    public String prompt(String message) {
        write(message);
        return in.nextLine();
    }

    /**
     * Prints the message and awaits user input.
     * If available, the typed input will be obscured.
     * @param message Message to prompt for input.
     * @return The input string.
     */
    public String promptPassword(String message) {
        write(message);
        if (systemConsole != null) {
            char[] inputArray = systemConsole.readPassword();
            String input = "";
            if (inputArray != null) {
                input = new String(inputArray);
                Arrays.fill(inputArray, ' ');
            }
            return input;
        }
        return in.nextLine();
    }

    @Override
    public void close() {
        in.close();
    }

    private void invokeClear() {
        try {
            Process process = new ProcessBuilder(CLEAR_COMMAND)
                    .inheritIO()
                    .start();
            process.waitFor();
        }
        catch (IOException ex) {
            clearUgly();
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void clearUgly() {
        writeLine("\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n" +
                        "\n\n\n\n\n\n\n\n\n\n");
    }

    private static String[] resolveClearCommand() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return new String[] { "cmd", "/c", "cls" };
        }
        return new String[] { "clear" };
    }
}
