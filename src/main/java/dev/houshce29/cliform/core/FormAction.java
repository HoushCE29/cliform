package dev.houshce29.cliform.core;

/**
 * Action to perform, such as re-asking for the current prompt or
 * flowing to a new form.
 */
public final class FormAction {
    private final Type type;
    private final String value;

    private FormAction(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    private FormAction(Type type) {
        this(type, type.name());
    }

    /**
     * @return The action type.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return Value for the action.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return <code>true</code> if this action disrupts element rendering.
     */
    public boolean isDisruptive() {
        return type == Type.EXIT
                || type == Type.FLOW_TO_FORM;
    }

    /**
     * @return <code>true</code> if this action forces the prompt to repeat.
     */
    public boolean repeatPrompt() {
        return type == Type.REPEAT_PROMPT;
    }

    /**
     * An action that tells the engine to flow to the given form.
     * @param formId ID of the form to go to.
     * @return Form action object renderable by the engine.
     */
    public static FormAction goTo(String formId) {
        return new FormAction(Type.FLOW_TO_FORM, formId);
    }

    /**
     * An action that tells the engine to reprompt.
     * @return Form action object renderable by the engine.
     */
    public static FormAction reprompt() {
        return new FormAction(Type.REPEAT_PROMPT);
    }

    /**
     * An action that tells the engine to exit.
     * @return Form action object renderable by the engine.
     */
    public static FormAction exit() {
        return new FormAction(Type.EXIT);
    }

    /**
     * An action that tells the engine to do nothing.
     * This is useful for input actions where handling is
     * purely just saving variables or throwing an exception
     * for application-breaking errors.
     * @return Form action object renderable by the engine.
     */
    public static FormAction noop() {
        return new FormAction(Type.NOOP);
    }

    public enum Type {
        FLOW_TO_FORM, REPEAT_PROMPT, EXIT, NOOP
    }
}
