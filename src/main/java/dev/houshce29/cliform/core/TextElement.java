package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * A simple element that displays text.
 */
public class TextElement implements Element {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void render(Map<String, Object> context, Console console) {
        console.writeLine(text);
    }
}
