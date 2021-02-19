package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * Special element that's rendered differently to accommodate for
 * text input handling.
 */
public class InputElement extends ActionableElement {
    private final boolean obscured;
    private final String prompt;
    private final InputHandler handler;

    public InputElement(boolean obscured, String prompt, InputHandler handler) {
        this.obscured = obscured;
        this.prompt = prompt;
        this.handler = handler;
    }

    public boolean isObscured() {
        return obscured;
    }

    public String getPrompt() {
        return prompt;
    }

    public InputHandler getHandler() {
        return handler;
    }

    @Override
    public FormAction renderWithAction(Map<String, Object> context, Console console) {
        String input;
        if (obscured) {
            input = console.promptPassword(prompt);
        }
        else {
            input = console.prompt(prompt);
        }
        return handler.onInput(input, context, console);
    }
}
