package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.core.Console;
import dev.houshce29.cliform.core.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TextElementGeneratorTest {
    private Console console;

    @Before
    public void beforeEach() {
        console = Mockito.mock(Console.class);
    }

    @Test
    public void testGenerate() {
        TextElementGenerator generator = new TextElementGenerator("Hello, world!", Collections.emptyList());
        Element element = generator.generate(null, Collections.emptyMap());
        element.render(Collections.emptyMap(), console);
        Mockito.verify(console).writeLine("Hello, world!");
    }

    @Test
    public void testGenerateWithArgs() {
        Map<String, Object> context = new HashMap<>();
        context.put("name", "houshce29");
        context.put("quantity", 60);
        context.put("description", "chickens");
        TextElementGenerator generator = new TextElementGenerator("%s has %s %s. %s",
                Arrays.asList("name", "quantity", "description", "Wow! That's a lot!"));
        Element element = generator.generate(null, context);
        element.render(context, console);
        Mockito.verify(console).writeLine("houshce29 has 60 chickens. Wow! That's a lot!");
    }
}
