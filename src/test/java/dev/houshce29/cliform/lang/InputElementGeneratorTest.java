package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.core.Console;
import dev.houshce29.cliform.core.FormAction;
import dev.houshce29.cliform.core.InputElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InputElementGeneratorTest {
    private static final String PROMPT = "Input: ";
    private static final String INPUT = "inputString";
    private Map<String, Object> context;
    private Console console;
    private TestForm form;

    @Before
    public void beforeEach() {
        context = new HashMap<>();
        console = Mockito.mock(Console.class);
        Mockito.when(console.prompt(Mockito.anyString())).thenReturn(INPUT);
        form = Mockito.spy(new TestForm());
    }

    @Test
    public void testGenerate() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "noopHandle", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        element.renderWithAction(context, console);
        Mockito.verify(form).noopHandle();
    }

    @Test
    public void testGenerateWithFormatArgs() {
        context.put("name", "houshce29");
        context.put("food", "tacos");
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "noopHandle", "%s likes %s", Arrays.asList("name", "food"), false);
        InputElement element = (InputElement) gen.generate(form, context);
        element.renderWithAction(context, console);
        Mockito.verify(console).prompt("houshce29 likes tacos");
    }

    @Test
    public void testGenerateWithFormAction() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "formHandle", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        FormAction action = element.renderWithAction(context, console);
        Assert.assertEquals(FormAction.Type.FLOW_TO_FORM, action.getType());
        Mockito.verify(form).formHandle();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateWithHandlerInvalidArgs() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "invalidArgsHandle", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        element.renderWithAction(context, console);
    }

    @Test
    public void testGenerateWithHandlerArgs() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "argsHandle", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        element.renderWithAction(context, console);
        Mockito.verify(form).argsHandle(INPUT, context, console);
    }

    @Test
    public void testGenerateNoHandler() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, null, PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        FormAction action = element.renderWithAction(context, console);
        Assert.assertEquals(FormAction.Type.NOOP, action.getType());
    }

    @Test
    public void testGenerateWithNonFormActionHandler() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "nonAction", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        FormAction action = element.renderWithAction(context, console);
        Assert.assertEquals(FormAction.Type.NOOP, action.getType());
        Mockito.verify(form).nonAction();
    }

    @Test
    public void testGenerateWithMissingMethod() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "nonEXISTENT**", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        FormAction action = element.renderWithAction(context, console);
        Assert.assertEquals(FormAction.Type.NOOP, action.getType());
    }

    @Test(expected = IllegalStateException.class)
    public void testGenerateErrorOnElementRender() {
        InputElementGenerator gen = new InputElementGenerator(
                TestForm.class, "handlerWithError", PROMPT, Collections.emptyList(), false);
        InputElement element = (InputElement) gen.generate(form, context);
        element.renderWithAction(context, console);
    }

    private static class TestForm {
        void noopHandle() {
        }

        void invalidArgsHandle(String input, Map<String, Object> context, Console console, int i,
                                boolean b, char c, InputElementGeneratorTest object) {
        }

        void argsHandle(String input, Map<String, Object> context, Console console) {
        }

        FormAction formHandle() {
            return FormAction.goTo("the-next-form");
        }

        boolean nonAction() {
            return true;
        }

        void handlerWithError() {
            throw new RuntimeException();
        }
    }
}
