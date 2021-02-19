package dev.houshce29.cliform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contains the form template written in Form Template Language format.
 * The template can be defined in one of three ways:
 *   <ol>
 *       <li>
 *           Value - The literal Form Template Language written in this annotation (as an array of strings
 *                   where each element is a new line).
 *       </li>
 *       <li>
 *           Resource - The Java resource path to the template text file to read from the current thread's class loader.
 *       </li>
 *       <li>
 *           Path - The path to the template text file. Only use this if the file is on the host machine's file system
 *                  (e.g. it's not located within a zipped/compressed file such as a JAR).
 *       </li>
 *   </ol>
 *
 * In the order above, the template will be resolved on the first non-empty property.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {
    /**
     * The hard-coded template. Each element in this array represents a new line.
     * @return The hard coded template.
     */
    String[] value() default {};

    /**
     * The resource path to the template file.
     * @return The resource path.
     */
    String resource() default "";

    /**
     * The filesystem path to the template file.
     * @return The path to the template file.
     */
    String path() default "";
}
