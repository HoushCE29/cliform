package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * Marker interface for elements.
 */
public interface Element {

    /**
     * Renders this element.
     * @param context The context.
     * @param console The console being rendered into.
     */
    void render(Map<String, Object> context, Console console);
}
