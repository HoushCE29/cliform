package dev.houshce29.cliform;

import dev.houshce29.cliform.core.AbstractForm;
import dev.houshce29.cliform.core.Element;
import dev.houshce29.cliform.lang.ElementGenerator;
import dev.houshce29.cliform.lang.TemplateParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extend this class for smart features. The impl of this
 * should have a Template annotation on it for reading.
 */
public abstract class SmartForm extends AbstractForm {
    private final List<ElementGenerator> generators;

    public SmartForm(String id) {
        super(id);
        this.generators = parse(this.getClass());
    }

    @Override
    public List<Element> create(Map<String, Object> context) {
        return generators.stream()
                .map(generator -> generator.generate(this, context))
                .collect(Collectors.toList());
    }

    private static List<ElementGenerator> parse(Class<?> impl) {
        TemplateParser parser = new TemplateParser();
        return parser.parse(impl);
    }
}
