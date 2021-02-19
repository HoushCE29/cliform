package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.core.Element;

import java.util.Map;

/**
 * Supplier-type functional interface to build an element.
 */
@FunctionalInterface
public interface ElementGenerator {

    /**
     * Creates an element.
     * @param source The source object hosting the element.
     * @param context Application context.
     * @return New element instance for use in forms.
     */
    Element generate(Object source, Map<String, Object> context);
}
