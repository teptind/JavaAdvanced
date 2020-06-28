package ru.ifmo.rain.teptin.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * SourceCreator for interfaces
 *
 * @author Daniil Teptin
 */

class SourceCreator {
    /** Default Constructor.
     *  No default behaviour as the class has no fields
     */
    public SourceCreator() {}
    /**
     * Generate code for class, implements interface
     *
     * @param token {@link Class} of interface to implement
     * @return {@link String} full realisation of class with package, header and methods
     */
    static String generateSource(Class<?> token) {
        return collectLine("",
                generateUsage(token),
                StringConstants.NEWLINE,
                generateHeader(token),
                StringConstants.OPENBLOCK,
                generateAllMethods(token),
                StringConstants.CLOSEBLOCK);
    }

    /**
     * Generate all {@link Method}, exists in interface
     *
     * @param token {@link Class} of interface to implement
     * @return {@link String} concatenated realisation of all {@link Method}s
     * @see SourceCreator#generateMethod(Method)
     */
    private static String generateAllMethods(Class<?> token) {
        return Arrays.stream(token.getMethods()).map(SourceCreator::generateMethod).collect(Collectors.joining(StringConstants.NEWLINE));
    }

    /**
     * Generates {@link String} with realisation of {@link Method}
     *
     * @param method {@link Method} to get realisation
     * @return {@link String} of {@link Method} with fill header and body
     */
    private static String generateMethod(Method method) {
        return collectLine(StringConstants.NEWLINE, generateMethodHeader(method), generateMethodBody(method));
    }

    /**
     * Forms the body of the {@link Method} declared in the interface
     *
     * @param method {@link Method} to generate body
     * @return {@link StringBuilder}: {@link Method} body
     */
    private static String generateMethodBody(Method method) {
        StringBuilder res = new StringBuilder("return");
        Class<?> resultType = method.getReturnType();
        if (resultType != void.class) {
            res.append(StringConstants.WHITESPACE);
            if (resultType.isPrimitive()) {
                if (resultType == boolean.class) {
                    res.append("false");
                } else {
                    res.append("0");
                }
            } else {
                res.append("null");
            }
        }
        return collectLine("",
                StringConstants.OPENBLOCK,
                res.toString(),
                StringConstants.SEMICOLON,
                StringConstants.CLOSEBLOCK);
    }

    /**
     * Forms the title of the {@link Method} declared in the interface
     *
     * @param method {@link Method} to generate header
     * @return {@link StringBuilder}: {@link Method} opening line
     */
    private static String generateMethodHeader(Method method) {
        StringBuilder res = new StringBuilder();
        res.append(collectLine(StringConstants.WHITESPACE,
                "public", method.getReturnType().getCanonicalName(), method.getName()));
        res.append('(');
        ArgNameGenerator argNameGenerator = new ArgNameGenerator();
        ArgNameGenerator.reset();
        res.append(Arrays.stream(method.getParameterTypes()).
                map(arg -> collectLine(" ", arg.getCanonicalName(), argNameGenerator.get())).
                collect(Collectors.joining(StringConstants.COMMA + StringConstants.WHITESPACE)));
        res.append(')');
        res.append(StringConstants.WHITESPACE);
        res.append(generateMethodExceptions(method));
        return res.toString();
    }

    /**
     * Generates {@link String} of a list of {@link Exception} that the method may <code>throw</code>.
     *
     * @param method {@link Method} for which you want to generate a {@link String} of thrown erroneously
     * @return {@link String} of {@link Exception}, delimited by coma and space
     */
    private static String generateMethodExceptions(Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length == 0) {
            return "";
        }
        return collectLine(" ", "throws", Arrays.stream(exceptionTypes).
                map(Class::getCanonicalName).
                collect(Collectors.joining(StringConstants.COMMA + StringConstants.WHITESPACE)));
    }

    /**
     * Generates a {@link String} - header of implemented class
     * @param token {@link Class} of interface to implement
     * @return header of the class {@link String}
     */
    private static String generateHeader(Class<?> token) {
        return collectLine(StringConstants.WHITESPACE,"public class", token.getSimpleName() + StringConstants.SUFFIX, "implements", token.getCanonicalName());
    }

    /**
     * Generates a {@link String} - package name of implemented class
     * @param token {@link Class} of interface to implement
     * @return package name {@link String}
     */
    private static String generateUsage(Class<?> token) {
        String packageName = token.getPackageName();
        return packageName == null ? "" : collectLine(StringConstants.WHITESPACE,"package", packageName) + StringConstants.SEMICOLON;
    }

    /**
     * Returns a {@link String} consisting of arguments passed, separated by a separator
     * @param separator separator that will separate arguments
     * @param args array of {@link String} to concatenate
     * @return Collected {@link String}
     */
    private static String collectLine(String separator, String... args) {
        return String.join(separator, args);
    }

    /**
     * Auxiliary class to generate variables names
     * <p>
     * {@link Supplier}, witch returns uniq names before reset
     */
    private static class ArgNameGenerator implements Supplier<String> {
        static int num = 0;
        static void reset() {
            num = 0;
        }

        @Override
        public String get() {
            return "arg" + (num++);
        }
    }
}
