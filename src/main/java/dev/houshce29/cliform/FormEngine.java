package dev.houshce29.cliform;

import dev.houshce29.cliform.core.ActionableElement;
import dev.houshce29.cliform.core.Console;
import dev.houshce29.cliform.core.Element;
import dev.houshce29.cliform.core.Form;
import dev.houshce29.cliform.core.FormAction;
import dev.houshce29.cliform.util.CollectionUtils;
import dev.houshce29.cliform.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main driver of form flows.
 */
public class FormEngine {
    private final Console console = new Console();
    private final String title;
    private final Map<String, Object> context = new ConcurrentHashMap<>();
    private final Map<String, Form> forms;

    private FormEngine(Builder builder) {
        this.title = createTitle(builder.applicationName, builder.applicationVersion);
        this.forms = CollectionUtils.toIdentityMap(builder.forms, Form::getId);
        this.context.putAll(builder.init);
    }

    /**
     * Starts and runs the engine.
     * @param initialFormId The form to start at.
     */
    public void start(String initialFormId) {
        String formId = initialFormId;
        // Run while there's a next form.
        while (StringUtils.isNotBlank(formId)) {
            formId = run(formId)
                    // Only care about form flow. The exit action will just break this loop.
                    .filter(action -> action.getType() == FormAction.Type.FLOW_TO_FORM)
                    .map(FormAction::getValue)
                    .orElse(null);
        }
    }

    /**
     * Runs the form of the given ID.
     * @param formId ID of the form to run.
     * @return An optional that might contain a form action that disrupts
     *         the current run (i.e. an exit or a form flow).
     */
    private Optional<FormAction> run(String formId) {
        // Load the form
        console.clear();
        renderAppTitle();
        Form form = loadForm(formId);
        form.onInit(context);
        // Render the elements
        for (Element element : form.create(context)) {
            Optional<FormAction> result = render(element);
            // If present at this level, it means that we'll need to
            // completely unwind and flow to the next form or exit.
            if (result.isPresent()) {
                form.onDestroy(context);
                return result;
            }
        }
        // End of the form, so run destroy logic
        form.onDestroy(context);
        return Optional.empty();
    }

    /**
     * Renders the given element.
     * @param element Element to render.
     * @return An optional that might contain a form action that disrupts the
     *         current rendering.
     */
    private Optional<FormAction> render(Element element) {
        // Always invoke render
        element.render(context, console);
        // Handle actionable element if applicable
        if (element instanceof ActionableElement) {
            Optional<FormAction> action = handleActionElement((ActionableElement) element);
            if (action.isPresent()) {
                return action;
            }
        }
        return Optional.empty();
    }

    /**
     * Handles an actionable element.
     * @param actionElement Element that has an action.
     * @return An optional that might contain a form action that disrupts the
     *         current rendering.
     */
    private Optional<FormAction> handleActionElement(ActionableElement actionElement) {
        FormAction action = actionElement.renderWithAction(context, console);
        if (action.isDisruptive()) {
            return Optional.of(action);
        }
        if (action.repeatPrompt()) {
            Optional<FormAction> result = repeatPrompt(actionElement);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * Runs the prompt over and over until not longer applicable.
     * @param element Actionable element to re-render while instructed.
     * @return An optional that might contain a form action that disrupts the
     *         current rendering.
     */
    private Optional<FormAction> repeatPrompt(ActionableElement element) {
        FormAction action = FormAction.reprompt();
        while (action.repeatPrompt()) {
            action = element.renderWithAction(context, console);
        }
        return Optional.ofNullable(action.isDisruptive() ? action : null);
    }

    /**
     * Loads the given form.
     * @param formId ID of the form.
     * @return Form instance to render.
     */
    private Form loadForm(String formId) {
        final Form form = forms.get(formId);
        if (form == null) {
            throw new IllegalArgumentException("No form of ID [" + formId + "] exists.");
        }
        return form;
    }

    /**
     * renders the primary app title if applicable.
     */
    private void renderAppTitle() {
        if (!title.isEmpty()) {
            console.writeLine(title);
            console.newLine();
        }
    }

    /**
     * Returns a new form engine builder. The context passed into
     * here will be passed throughout the framework to act as a
     * payload/DTO between forms.
     * @return A new builder instance.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private static String createTitle(String appName, String appVer) {
        String title = "";
        if (StringUtils.isNotBlank(appName)) {
            title = appName;
        }
        if (StringUtils.isNotBlank(appVer)) {
            title = (StringUtils.isNotBlank(title) ? title + " - " : "")
                    .concat(appVer);
        }
        return title;
    }

    /**
     * Builder for building the form engine.
     */
    public static class Builder {
        private String applicationName;
        private String applicationVersion;
        private final Map<String, Object> init = new HashMap<>();
        private final List<Form> forms = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets the application name.
         * @param applicationName Name of the application.
         * @return This builder.
         */
        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        /**
         * Sets the application version.
         * @param applicationVersion Version of the application.
         * @return This builder.
         */
        public Builder setApplicationVersion(String applicationVersion) {
            this.applicationVersion = applicationVersion;
            return this;
        }

        /**
         * Adds all the forms to the engine being built.
         * @param forms Forms to include.
         * @return This builder.
         */
        public Builder addForms(List<Form> forms) {
            this.forms.addAll(forms);
            return this;
        }

        /**
         * Adds all the forms to the engine being built.
         * @param forms Forms to include.
         * @return This builder.
         */
        public Builder addForms(Form... forms) {
            return addForms(Arrays.asList(forms));
        }

        /**
         * Inserts an initial context value into the engine.
         * @param key Key to identify.
         * @param value Value.
         * @return This builder.
         */
        public Builder setContextValue(String key, Object value) {
            init.put(key, value);
            return this;
        }

        /**
         * Inserts all the initial context values into the engine.
         * @param values Values to insert.
         * @return This builder.
         */
        public Builder addContextValues(Map<String, Object> values) {
            init.putAll(values);
            return this;
        }

        /**
         * Builds the form engine.
         * @return A new form engine instance configured by this builder.
         */
        public FormEngine build() {
            return new FormEngine(this);
        }
    }
}
