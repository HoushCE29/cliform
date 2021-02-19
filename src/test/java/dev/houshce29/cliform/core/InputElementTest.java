package dev.houshce29.cliform.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

public class InputElementTest {
    private static final String PROMPT = "Type something: ";
    private static final String INPUT = "input";
    private static final Map<String, Object> CONTEXT = Collections.emptyMap();
    private InputHandler handler;
    private Console console;

    @Before
    public void beforeEach() {
        handler = Mockito.mock(InputHandler.class);
        console = Mockito.mock(Console.class);
        Mockito.when(console.prompt(PROMPT)).thenReturn(INPUT);
        Mockito.when(console.promptPassword(PROMPT)).thenReturn(INPUT);
    }

    @Test
    public void testRenderWithAction() {
        InputElement element = new InputElement(false, PROMPT, handler);
        element.renderWithAction(CONTEXT, console);
        Mockito.verify(console).prompt(PROMPT);
        Mockito.verify(console, Mockito.never()).promptPassword(PROMPT);
        Mockito.verify(handler).onInput(INPUT, CONTEXT, console);
    }

    @Test
    public void testRenderWithActionObscured() {
        InputElement element = new InputElement(true, PROMPT, handler);
        element.renderWithAction(CONTEXT, console);
        Mockito.verify(console, Mockito.never()).prompt(PROMPT);
        Mockito.verify(console).promptPassword(PROMPT);
        Mockito.verify(handler).onInput(INPUT, CONTEXT, console);
    }
}
