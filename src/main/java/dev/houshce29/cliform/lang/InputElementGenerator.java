package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.core.Console;
import dev.houshce29.cliform.core.Element;
import dev.houshce29.cliform.core.FormAction;
import dev.houshce29.cliform.core.InputElement;
import dev.houshce29.cliform.util.StringUtils;
import dev.houshce29.cliform.util.VisibleForTesting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Input element generator impl.
 */
public class InputElementGenerator implements ElementGenerator {
    private final Method handler;
    private final String promptFormat;
    private final List<String> promptFormatArgs;
    private final boolean obscure;

    InputElementGenerator(Class<?> sourceClass,
                          String handlerRef,
                          String promptFormat,
                          List<String> promptArgs,
                          boolean obscure) {
        this.handler = resolveMethod(sourceClass, handlerRef);
        this.promptFormat = promptFormat;
        this.promptFormatArgs = promptArgs;
        this.obscure = obscure;
    }

    public String getPromptFormat() {
        return promptFormat;
    }

    public List<String> getPromptFormatArgs() {
        return Collections.unmodifiableList(promptFormatArgs);
    }

    public boolean isObscure() {
        return obscure;
    }

    @VisibleForTesting
    Method getHandler() {
        return handler;
    }

    @Override
    public Element generate(Object source, Map<String, Object> context) {
        Object[] formatArgs = promptFormatArgs.stream()
                .map(arg -> context.getOrDefault(arg, arg))
                .toArray();
        final String prompt = String.format(promptFormat, formatArgs);
        return new InputElement(obscure, prompt, (in, ctx, console) -> invokeAction(source, in, ctx, console));
    }

    private FormAction invokeAction(Object source, String input, Map<String, Object> context, Console console) {
        // No handler, so return a noop action.
        if (handler == null) {
            return FormAction.noop();
        }
        Object[] args = createArgs(input, context, console, handler.getParameterTypes());
        return invokeHandlerMethod(source, args);
    }

    private FormAction invokeHandlerMethod(Object source, Object[] args) {
        try {
            // Reflectively invoke the method
            Object out = handler.invoke(source, args);
            // Return if the return value is an instance of FormAction
            if (out instanceof FormAction) {
                return (FormAction) out;
            }
            // Otherwise consider it a NOOP action
            return FormAction.noop();
        }
        catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to invoke " + handler.getName() + ".", ex);
        }
    }

    private static Object[] createArgs(String input,
                                       Map<String, Object> context,
                                       Console console,
                                       Class... paramTypes) {
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = createArg(paramTypes[i], input, context, console);
        }
        return args;
    }

    private static Object createArg(Class paramType, String input, Map<String, Object> context, Console console) {
        if (String.class.isAssignableFrom(paramType)) {
            return input;
        }
        else if (Map.class.isAssignableFrom(paramType)) {
            return context;
        }
        else if (Console.class.isAssignableFrom(paramType)) {
            return console;
        }
        throw new IllegalArgumentException("Handler method arguments can only be of type String, Map<String, Object>, or Console." );
    }

    private static Method resolveMethod(Class<?> clazz, String methodName) {
        if (StringUtils.isBlank(methodName)) {
            return null;
        }
        Method out = Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseGet(() -> Arrays.stream(clazz.getDeclaredMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .findFirst()
                        .orElse(null));
        if (out != null) {
            out.setAccessible(true);
        }
        return out;
    }
}
