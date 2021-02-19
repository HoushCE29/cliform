package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * An actionable element, which renders differently than a
 * normal display-style element.
 */
public abstract class ActionableElement implements Element {

    @Override
    public void render(Map<String, Object> context, Console console) {
        // Do nothing.
        // The engine will call the abstract method below instead.
    }

    /**
     * Renders this element and returns a followup action.
     * @param context Application context.
     * @param console Console to print to if necessary.
     * @return A form action for the engine to perform.
     */
    public abstract FormAction renderWithAction(Map<String, Object> context, Console console);
}
