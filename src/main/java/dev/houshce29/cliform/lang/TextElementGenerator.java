package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.core.Element;
import dev.houshce29.cliform.core.TextElement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Element generator impl for a text element.
 */
public class TextElementGenerator implements ElementGenerator {
    private final String format;
    private final List<String> formatContextArgs;

    TextElementGenerator(String format, List<String> formatContextArgs) {
        this.format = format;
        this.formatContextArgs = formatContextArgs;
    }

    public String getFormat() {
        return format;
    }

    public List<String> getFormatContextArgs() {
        return Collections.unmodifiableList(formatContextArgs);
    }

    @Override
    public Element generate(Object source, Map<String, Object> context) {
        Object[] formatArgs = formatContextArgs.stream()
                .map(arg -> context.getOrDefault(arg, arg))
                .toArray();
        return new TextElement(String.format(format, formatArgs));
    }
}
