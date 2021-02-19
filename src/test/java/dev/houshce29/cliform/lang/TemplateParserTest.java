package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.Template;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

public class TemplateParserTest {
    private TemplateParser parser;

    @Before
    public void beforeEach() {
        parser = Mockito.spy(new TemplateParser());
    }

    @Test
    public void testParseByTemplateClass() {
        Mockito.doReturn(null).when(parser).parse(Mockito.any(), Mockito.any(Template.class));
        parser.parse(HardCoded.class);
        Mockito.verify(parser).parse(Mockito.any(), Mockito.any(Template.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNoTemplateOnSource() {
        parser.parse(Object.class);
    }

    @Test
    public void testParseTemplateValue() {
        Mockito.doReturn(null).when(parser).parse(Mockito.any(), Mockito.anyList());
        parser.parse(HardCoded.class, HardCoded.class.getAnnotation(Template.class));
        Mockito.verify(parser).parse(HardCoded.class, Arrays.asList(
                "Hello, world!",
                "My name is ${name}.",
                "What is your name? [readName]"));
    }

    @Test
    public void testParseTemplateResource() {
        Mockito.doReturn(null).when(parser).parse(Mockito.any(), Mockito.anyList());
        parser.parse(FromResource.class, FromResource.class.getAnnotation(Template.class));
        Mockito.verify(parser).parse(FromResource.class, Arrays.asList(
                "Hello, world!",
                "My name is ${name}.",
                "What is your name? [readName]"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNoTemplateDef() {
        parser.parse(BadTemplate.class, BadTemplate.class.getAnnotation(Template.class));
    }

    @Test
    public void testParseLines() {
        Mockito.doReturn(null).when(parser).parseLine(Mockito.any(), Mockito.anyInt(), Mockito.anyString());
        parser.parse(HardCoded.class, Arrays.asList("1", "2", "3", "4"));
        Mockito.verify(parser, Mockito.times(4))
                .parseLine(Mockito.any(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testParseLineSimple() {
        final String simple = "This is a line of text.";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, simple);
        Assert.assertEquals(TextElementGenerator.class, generator.getClass());
        TextElementGenerator textGen = (TextElementGenerator) generator;
        Assert.assertEquals(simple, textGen.getFormat());
    }

    @Test
    public void testParseLineWithVariable() {
        final String vars = "And $yourName when you call me, you can call me $myName";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, vars);
        Assert.assertEquals(TextElementGenerator.class, generator.getClass());
        TextElementGenerator gen = (TextElementGenerator) generator;
        Assert.assertEquals("And %s when you call me, you can call me %s", gen.getFormat());
        Assert.assertEquals(Arrays.asList("yourName", "myName"), gen.getFormatContextArgs());
    }

    @Test
    public void testParseLineWithEscapedVariableInitSymbol() {
        final String text = "I have $ in the amount of $$0.01. It ain't much, but it's $";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, text);
        Assert.assertEquals(TextElementGenerator.class, generator.getClass());
        TextElementGenerator gen = (TextElementGenerator) generator;
        Assert.assertEquals("I have $ in the amount of $0.01. It ain't much, but it's $", gen.getFormat());
        Assert.assertTrue(gen.getFormatContextArgs().isEmpty());
    }

    @Test
    public void testParseLineWithVariableBlock() {
        final String text = "Taco selection: ${shellType}, ${meatType} (${toppings})";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, text);
        Assert.assertEquals(TextElementGenerator.class, generator.getClass());
        TextElementGenerator gen = (TextElementGenerator) generator;
        Assert.assertEquals("Taco selection: %s, %s (%s)", gen.getFormat());
        Assert.assertEquals(Arrays.asList("shellType", "meatType", "toppings"), gen.getFormatContextArgs());
    }

    @Test
    public void testParseLineWithJavaFormatSymbol() {
        final String text = "The % of taxes taken by the government is 100% theft no matter the intent.";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, text);
        Assert.assertEquals(TextElementGenerator.class, generator.getClass());
        TextElementGenerator gen = (TextElementGenerator) generator;
        Assert.assertEquals(text.replaceAll("%", "%%"), gen.getFormat());
    }

    @Test
    public void testParseLineWithInputBinding() throws Exception {
        final String prompt = "Hello, ${name}. What is your favorite color? [readInput]";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, prompt);
        Assert.assertEquals(InputElementGenerator.class, generator.getClass());
        InputElementGenerator gen = (InputElementGenerator) generator;
        Assert.assertFalse(gen.isObscure());
        Assert.assertEquals("Hello, %s. What is your favorite color? ", gen.getPromptFormat());
        Assert.assertEquals(Collections.singletonList("name"), gen.getPromptFormatArgs());
        Assert.assertEquals(MyForm.class.getMethod("readInput", String.class), gen.getHandler());
    }

    @Test
    public void testParseLineWithInputBindingHidden() throws Exception {
        final String prompt = "Password: [!readInput]";
        ElementGenerator generator = parser.parseLine(MyForm.class, 1, prompt);
        Assert.assertEquals(InputElementGenerator.class, generator.getClass());
        InputElementGenerator gen = (InputElementGenerator) generator;
        Assert.assertTrue(gen.isObscure());
        Assert.assertEquals("Password: ", gen.getPromptFormat());
        Assert.assertEquals(MyForm.class.getMethod("readInput", String.class), gen.getHandler());
    }

    @Test(expected = IllegalStateException.class)
    public void testParseUnexpectedPostHandlerRefTokens() {
        parser.parseLine(MyForm.class, 1, "Input: [readInput] HOWDY");
    }

    @Test(expected = IllegalStateException.class)
    public void testParseIncompleteVariableBlock() {
        parser.parseLine(MyForm.class, 1, "My name is ${name");
    }

    @Test(expected = IllegalStateException.class)
    public void testParseIncompleteHandlerRefBlock() {
        parser.parseLine(MyForm.class, 1, "Password: [read");
    }

    @Template({
            "Hello, world!",
            "My name is ${name}.",
            "What is your name? [readName]"
    })
    public static class HardCoded {
    }

    @Template(resource = "template.txt")
    public static class FromResource {
    }

    @Template
    public static class BadTemplate {
    }

    public static class MyForm {
        public void readInput(String input) { }
    }
}
