package dev.houshce29.cliform.core;

import java.util.Map;

/**
 * Abstract form to take care of boilerplate stuff.
 */
public abstract class AbstractForm implements Form {
    private final String id;

    public AbstractForm(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void onInit(Map<String, Object> context) {
        // By default, stub this to do nothing.
    }

    @Override
    public void onDestroy(Map<String, Object> context) {
        // By default, stub this to do nothing.
    }
}
