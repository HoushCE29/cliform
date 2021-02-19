package dev.houshce29.cliform.lang;

import dev.houshce29.cliform.Template;
import dev.houshce29.cliform.util.CollectionUtils;
import dev.houshce29.cliform.util.IOUtils;
import dev.houshce29.cliform.util.StringUtils;
import dev.houshce29.cliform.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A parser that parses template language.
 */
public class TemplateParser {

    /**
     * Parses the template attached to the given class, and binds
     * the template to the class.
     * @param source Source class hosting the template.
     * @return List of element generators.
     */
    public List<ElementGenerator> parse(Class<?> source) {
        Template template = source.getAnnotation(Template.class);
        if (template == null) {
            throw new IllegalArgumentException(source + " does not have a template associated with it.");
        }
        return parse(source, template);
    }

    /**
     * Parses the template and binds it to the given class.
     * @param source Source class to bind the template to.
     * @param template Template to parse.
     * @return List of element generators.
     */
    public List<ElementGenerator> parse(Class<?> source, Template template) {
        List<String> lines;
        if (!CollectionUtils.isArrayEmpty(template.value())) {
            lines = Arrays.asList(template.value());
        }
        else if (StringUtils.isNotBlank(template.resource())) {
            lines = IOUtils.readResource(template.resource());
        }
        else if (StringUtils.isNotBlank(template.path())) {
            lines = IOUtils.readFile(template.path());
        }
        else {
            throw new IllegalArgumentException("No template source defined.");
        }
        return parse(source, lines);
    }

    /**
     * Parses the lines and binds them to the source class.
     * @param source Source form class hosting the template.
     * @param lines Lines within the template (e.g. split by line break char).
     * @return List of element generators.
     */
    public List<ElementGenerator> parse(Class<?> source, List<String> lines) {
        List<ElementGenerator> creators = new ArrayList<>();
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            creators.add(parseLine(source, lineNumber, line));
        }
        return creators;
    }

    @VisibleForTesting
    ElementGenerator parseLine(Class<?> source, int lineNumber, String line) {
        LineParseContext context = new LineParseContext();
        char[] chars = line.toCharArray();
        for (char current : chars) {
            if (context.state == ParseState.POST_HANDLER_REF && !Character.isWhitespace(current)) {
                throw new IllegalStateException("Unexpected token(s) after defined input on line " + lineNumber
                        + " in template for " + source + ".");
            }
            nextChar(current, context);
        }
        completeLineParsing(lineNumber, source, context);
        // This is an input line, so return it like that
        if (context.input) {
            return newInputCreator(source, context);
        }
        return new TextElementGenerator(context.format.toString(), context.formatContextArgs);
    }

    private static ElementGenerator newInputCreator(Class<?> source, LineParseContext context) {
        String ref = context.handler.toString();
        boolean obscure = false;
        if (ref.startsWith("!")) {
            obscure = true;
            ref = StringUtils.substringAfter(ref, "!");
        }
        return new InputElementGenerator(
                source, ref, context.format.toString(), context.formatContextArgs, obscure);
    }

    private static void nextChar(char current, LineParseContext context) {
        switch (context.state) {
            case LITERAL:
                nextCharLiteralState(current, context);
                break;
            case VARIABLE_INIT:
                nextCharVariableInitState(current, context);
                break;
            case VARIABLE_BLOCK:
                nextCharVariableBlockState(current, context);
                break;
            case VARIABLE:
                nextCharVariableState(current, context);
                break;
            case HANDLER_REF:
                nextCharHandlerRefState(current, context);
                break;
        }
    }

    private static void completeLineParsing(int lineNumber, Class<?> source, LineParseContext context) {
        switch (context.state) {
            // EOF means this is just a literal $
            case VARIABLE_INIT:
                context.format.append('$');
                break;
            // EOF concludes the current arg
            case VARIABLE:
                context.formatContextArgs.add(context.currentContextArg.toString());
                break;
            // Incomplete variable block
            case VARIABLE_BLOCK:
                throw new IllegalStateException("Incomplete variable block on line " + lineNumber
                        + " in template for " + source + ".");
            case HANDLER_REF:
                throw new IllegalStateException("Incomplete input block on line " + lineNumber
                        + " in template for " + source + ".");
        }
    }

    private static void nextCharLiteralState(char current, LineParseContext context) {
        // Start of a variable found
        if (current == '$') {
            context.state = ParseState.VARIABLE_INIT;
        }
        // Start of handler ref found
        else if (current == '[') {
            context.state = ParseState.HANDLER_REF;
        }
        // Special case: prevent injection / messing up of format
        else if (current == '%') {
            // Escape for Java string formatting
            context.format.append("%%");
        }
        // More literals to append to format
        else {
            context.format.append(current);
        }
    }

    private static void nextCharVariableInitState(char current, LineParseContext context) {
        // A second $ means escaped initial $, so flip back to literal
        if (current == '$') {
            context.state = ParseState.LITERAL;
            context.format.append(current);
        }
        // Whitespace right after $ is just a literal $, so record both
        // the $ and the whitespace
        else if (Character.isWhitespace(current)) {
            context.state = ParseState.LITERAL;
            context.format.append('$').append(current);
        }
        // Bounded variable (i.e. ${theVariable})
        else if (current == '{') {
            context.state = ParseState.VARIABLE_BLOCK;
            context.format.append("%s");
        }
        // Regular variable (i.e. $theVariable)
        else {
            context.state = ParseState.VARIABLE;
            context.currentContextArg.append(current);
            context.format.append("%s");
        }
    }

    private static void nextCharVariableBlockState(char current, LineParseContext context) {
        // End of block variable
        if (current == '}') {
            context.state = ParseState.LITERAL;
            context.formatContextArgs.add(context.currentContextArg.toString());
            context.currentContextArg = new StringBuilder();
        }
        // Continue name of variable
        else {
            context.currentContextArg.append(current);
        }
    }

    private static void nextCharVariableState(char current, LineParseContext context) {
        // Marks the end of a variable
        if (Character.isWhitespace(current)) {
            context.state = ParseState.LITERAL;
            context.formatContextArgs.add(context.currentContextArg.toString());
            context.currentContextArg = new StringBuilder();
            context.format.append(current);
        }
        else {
            context.currentContextArg.append(current);
        }
    }

    private static void nextCharHandlerRefState(char current, LineParseContext context) {
        // First always flag this as being an input line
        context.input = true;
        // Closing ] ends the ref
        if (current == ']') {
            context.state = ParseState.POST_HANDLER_REF;
        }
        else {
            context.handler.append(current);
        }
    }

    // -- Parser helpers:

    private static class LineParseContext {
        private ParseState state = ParseState.LITERAL;
        private StringBuilder format = new StringBuilder();
        private List<String> formatContextArgs = new ArrayList<>();
        private StringBuilder currentContextArg = new StringBuilder();
        private boolean input = false;
        private StringBuilder handler = new StringBuilder();
    }

    private enum ParseState {
        // any non-special literal
        LITERAL,
        // The start of a variable, which is upon reading "$"
        VARIABLE_INIT,
        // The state while non-whitespace characters are read.
        // e.g. anything after the $ in $myVariable
        VARIABLE,
        // The state in between bounded variables such as ${foobarbaz}
        VARIABLE_BLOCK,
        // The state in between the []'s like in [myHandlerMethod]
        HANDLER_REF,
        // A potential error state if non-blank chars detected after a handler ref
        POST_HANDLER_REF
    }
}
