package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * Function that handles input.
 */
@FunctionalInterface
public interface InputHandler {

    /**
     * Performs some arbitrary logic on input.
     * @param input Input passed in from user.
     * @param context Context of the application.
     * @param console Console for any necessary printing.
     * @return An action for the form to take, if any.
     */
    FormAction onInput(String input, Map<String, Object> context, Console console) throws RuntimeException;
}
