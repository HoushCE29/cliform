# CLIForm
A framework that provides a simple way to build command line applications.

## What is CLIForm?
CLIForm is a Java framework that builds views for command line applications in a format similar to Angular.

Traditionally, every new CLI Java application is filled with a bunch of `System.out` statements. The goal of this framework is to take away much of this boilerplate stuff and allow devs to focus on their content instead.

## Templates and Forms
A template is the design of the current form/view in the console. For the most part, the contents of the template will show up exactly as it is written.
A form contains a template and the logic around the template.

Within this framework, the template definition can either go in its own text file, or it can be hardcoded directly into the `@Template` annotation.
The annotation is then placed on the implementation class that extends `SmartForm`.

For example, a simple shopping list like this will appear as-is in the console:
```
Shopping List:

  1. 2% Milk (gal)
  2. Cheese
  3. Bread
  4. Butter
  5. Potatoes
```
In Java, this would look something like this:
```java
@Template({
    "Shopping List:",
    "",
    "  1. 2% Milk (gal)",
    "  2. Cheese",
    "  3. Bread",
    "  4. Butter",
    "  5. Potatoes"
})
public class ShoppingListView extends SmartForm {
    public ShoppingListView() {
        super("shopping-list-form");
    }
}
```
Or, alternatively, the contents can go in a text file in the project's `resources` and referenced in the `@Template` annotation instead:
```java
@Template(resource = "shopping-list-form.txt")
public class ShoppingListView extends SmartForm {
    public ShoppingListView() {
        super("shopping-list-form");
    }
}
```

The demonstrated example above is great for static, unchanging data. Now consider the needs of actual applications that have dynamic data.

Dynamic data can be displayed using variable-replacement notation. If the variable replacement is bound by whitespace, the notation is simply `$theVariableName`.
If there are other non-whitespace characters immediately next to the variable replacement, the variable name should be encapsulated in block notation: `${theVariableName}`.
If the `$` character is needed, simply double-sequence to escape it ( `$$` ), or use block notation around it ( `${$}` ). There is no need to escape the `$` if whitespace immediately follows.

If a variable's value is not found / is `null`, then the variable name will appear instead.

For example, given this template:
```
Welcome back, ${user.name}!

You have $$$amount in the bank.
You have $ donated to ${charity.name}.
Your favorite color is ${color}.
```
and these variables:
```
user.name = Bob
amount = 100,000
charity.name = Alzheimer's Research
color = null
```
the output to the console will be:
```
Welcome back, Bob!

You have $100,000 in the bank.
You have $ donated to Alzheimer's Research.
Your favorite color is color.
```

In Java, this would be something like this:
```java
@Template ({
    "Welcome back, ${user.name}!",
    "",
    "You have $$$amount in the bank.",
    "You have $ donated to ${charity.name}.",
    "Your favorite color is ${color}."
})
public class MyView extends SmartForm {
    public MyView() {
        super("my-view");
    }
    
    @Override
    public void onInit(Map<String, String> context) {
        // Or, these values can be put here by previous forms
        context.put("user.name", "Bob");
        context.put("amount", "100,000");
        context.put("charity.name", "Alzheimer's Research");
    }
}
```

Finally, an application typically has user interaction. For CLI applications, this is in the form of a user's keyboard input. User input can be piped into Java code by referencing the method that will handle it.
The name of the method should be wrapped in square brackets: `[doHandleInput]`. If the user input potentially contains sensitive information like a password, the method name should be prefixed with the bang ( `!` ) character: `[!handleReadPassword]`.
The method this references can return any type, but in order to control form flow (e.g. go to another form, re-prompt, exit, etc.), it should return an instance of `FormAction`. The inputs to the method can only be `Map<String, Object>` (the variable-value map), `String` (the user input), or `Console` (the console object being printed to).
If the square bracket characters need to be escaped, simple wrap it in block-variable notation: `${[}`.

A simple login example:
```
LOGIN

Username: [readUsername]
Password: [!readPassword]
```

In Java:
```java
@Template({
    "LOGIN",
    "",
    "Username: [readUsername]",
    "Password: [!readPassword]"
})
public class Login extends SmartForm {
    public Login() {
        super("login-view");
    }
    
    public void readUsername(String input, Map<String, String> context) {
        context.put("user", input);
    }
    
    public FormAction readPassword(String input, Map<String, String> context, Console console) {
        boolean success = LoginService.login(context.get("user"), input);
        if (!success) {
            console.writeLine("Invalid user credentials.");
            console.promptPassword("Press enter...");
            // Flow back to this form.
            return FormAction.goTo("login-view");
        }
        return FormAction.goTo("main-view");
    }
}
```

## Defining the Form Engine
Defining the form engine is quite simple. To do so, simply use the builder. A `FormEngine` instance isn't required, therefore it can be started from the desired starting form right away:
```java
FormEngine.newBuilder()
    .setApplicationName("Test Application")
    .setApplicationVerion("0.0.1-BETA")
    .addForms(new LoginForm(), new MainForm(), new EditForm(), ...)
    .setContextValue("some-variable", "initial-value")
    .build()
    .start("login-form");
```
