package dev.houshce29.cliform.core;

import java.util.List;
import java.util.Map;

/**
 * Represents a form.
 */
public interface Form {

    /**
     * @return The unique ID of the form.
     */
    String getId();

    /**
     * Runs logic before form creation.
     * @param context Application context.
     */
    void onInit(Map<String, Object> context);

    /**
     * Creates the form for the engine to run.
     * @param context Application context.
     * @return List of elements.
     */
    List<Element> create(Map<String, Object> context);

    /**
     * Runs logic after the form is done being interacted with.
     * @param context Application context.
     */
    void onDestroy(Map<String, Object> context);
}
