package dev.houshce29.cliform;

import dev.houshce29.cliform.core.AbstractForm;
import dev.houshce29.cliform.core.ActionableElement;
import dev.houshce29.cliform.core.Console;
import dev.houshce29.cliform.core.Element;
import dev.houshce29.cliform.core.FormAction;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FormEngineTest {

    @Test
    public void testFormLifeCycle() {
        DynamicForm form = new DynamicForm("FORM");
        FormEngine.newBuilder()
                .addForms(form)
                .build()
                .start(form.getId());
        Assert.assertTrue(form.isOnInitInvoked());
        Assert.assertTrue(form.isCreateInvoked());
        Assert.assertTrue(form.isOnDestroyInvoked());
    }

    @Test
    public void testFormFlow() {
        DynamicForm formA = new DynamicForm("A", new TestElement(FormAction.goTo("B")));
        DynamicForm formB = new DynamicForm("B");
        FormEngine.newBuilder()
                .addForms(formA, formB)
                .build()
                .start(formA.getId());
        Assert.assertTrue(formA.isOnInitInvoked());
        Assert.assertTrue(formA.isCreateInvoked());
        Assert.assertTrue(formA.isOnDestroyInvoked());
        Assert.assertTrue(formB.isOnInitInvoked());
        Assert.assertTrue(formB.isCreateInvoked());
        Assert.assertTrue(formB.isOnDestroyInvoked());
    }

    @Test
    public void testFormReprompt() {
        TestElement element = new TestElement(
                FormAction.reprompt(), FormAction.reprompt(), FormAction.reprompt());
        DynamicForm form = new DynamicForm("FORM", element);
        FormEngine.newBuilder()
                .addForms(form)
                .build()
                .start(form.getId());
        Assert.assertEquals(4, element.getInvocations());
    }

    @Test
    public void testFormNoop() {
        TestElement elementA = new TestElement(FormAction.noop());
        TestElement elementB = new TestElement(FormAction.noop());
        DynamicForm form = new DynamicForm("FORM", elementA, elementB);
        FormEngine.newBuilder()
                .addForms(form)
                .build()
                .start(form.getId());
        Assert.assertEquals(1, elementA.getInvocations());
        Assert.assertEquals(1, elementB.getInvocations());
    }

    @Test
    public void testFormExit() {
        TestElement elementA = new TestElement(FormAction.exit());
        TestElement elementB = new TestElement(FormAction.noop());
        DynamicForm form = new DynamicForm("FORM", elementA, elementB);
        FormEngine.newBuilder()
                .addForms(form)
                .build()
                .start(form.getId());
        Assert.assertEquals(1, elementA.getInvocations());
        // Should exit before this is rendered
        Assert.assertEquals(0, elementB.getInvocations());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadFormNotFound() {
        FormEngine.newBuilder().build().start("form");
    }

    private static class DynamicForm extends AbstractForm {
        private final List<Element> elements;
        private boolean onInitInvoked = false;
        private boolean createInvoked = false;
        private boolean onDestroyInvoked = false;

        public DynamicForm(String id, Element... elements) {
            super(id);
            this.elements = Arrays.asList(elements);
        }

        @Override
        public void onInit(Map<String, Object> context) {
            onInitInvoked = true;
        }

        @Override
        public void onDestroy(Map<String, Object> context) {
            onDestroyInvoked = true;
        }

        @Override
        public List<Element> create(Map<String, Object> context) {
            createInvoked = true;
            return elements;
        }

        public boolean isOnInitInvoked() {
            return onInitInvoked;
        }

        public boolean isCreateInvoked() {
            return createInvoked;
        }

        public boolean isOnDestroyInvoked() {
            return onDestroyInvoked;
        }
    }

    private static class TestElement extends ActionableElement {
        private final List<FormAction> actions;
        private int invocations = 0;
        public TestElement(FormAction... actions) {
            this.actions = Arrays.asList(actions);
        }

        public int getInvocations() {
            return invocations;
        }

        @Override
        public FormAction renderWithAction(Map<String, Object> context, Console console) {
            FormAction action = invocations < actions.size() ? actions.get(invocations) : FormAction.noop();
            invocations++;
            return action;
        }
    }
}
