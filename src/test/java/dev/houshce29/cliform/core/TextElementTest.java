package dev.houshce29.cliform.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TextElementTest {
    private static final String TEXT = "TEST_TEXT";
    private Console console;

    @Before
    public void beforeEach() {
        console = Mockito.mock(Console.class);
    }

    @Test
    public void testRender() {
        TextElement element = new TextElement(TEXT);
        element.render(null, console);
        Mockito.verify(console).writeLine(TEXT);
    }
}
