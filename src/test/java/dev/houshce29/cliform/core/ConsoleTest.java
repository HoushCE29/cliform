package dev.houshce29.cliform.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class ConsoleTest {
    private PrintStream out;
    private InputStream in;
    private Console console;

    @Before
    public void beforeEach() {
        out = Mockito.mock(PrintStream.class);
    }

    @Test
    public void testWrite() {
        console = new Console(out, new ByteArrayInputStream(new byte[0]), System.console());
        console.write("testWrite");
        Mockito.verify(out).print((Object) "testWrite");
    }

    @Test
    public void testNewLine() {
        console = new Console(out, new ByteArrayInputStream(new byte[0]), System.console());
        console.newLine();
        Mockito.verify(out).println();
    }

    @Test
    public void testWriteLines() {
        console = new Console(out, new ByteArrayInputStream(new byte[0]), System.console());
        console.writeLines(1, 2, 3, 4, 5);
        Mockito.verify(out, Mockito.times(5)).println(Mockito.any(Object.class));
    }

    @Test
    public void testPrompt() {
        final String input = "testPrompt";
        console = new Console(out, new ByteArrayInputStream(input.getBytes()), System.console());
        String output = console.prompt("Prompt: ");
        Assert.assertEquals(input, output);
        Mockito.verify(out).print((Object) "Prompt: ");
    }
}
