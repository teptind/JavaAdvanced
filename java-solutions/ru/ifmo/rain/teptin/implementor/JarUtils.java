package ru.ifmo.rain.teptin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class with special Utils for JarImplementor
 * @see JarImplementor
 */
class JarUtils {
    /** Default Constructor.
     *  No default behaviour as the class has no fields
     */
    public JarUtils() {}
    /**
     * Compiles .java code to .class file
     *
     * @param token Class token
     * @param sourceDir path to java code
     * @throws ImplerException whether compilation was failed
     */
    static void compile(Class<?> token, Path sourceDir) throws ImplerException {
        Path superPath = getSuperPath(token);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Cannot find any Java compiler");
        }
        String[] args = {"-encoding", "UTF-8", "-cp",
                sourceDir.toString() + File.pathSeparator + superPath.toString(),
                sourceDir.resolve(getPathToImplementation(token, File.separator) +
                        StringConstants.SUFFIX + StringConstants.JAVA_EXTENSION).toString()};
        int errorCode = compiler.run(null, null, null, args);
        if (errorCode != 0) {
            throw new ImplerException("Compilation failed: " + errorCode);
        }
    }

    /**
     * Creates JAR file
     *
     * @param token Class token
     * @param sourceDir directory with .class code
     * @param targetPath directory to save JAR file
     * @throws ImplerException whether JAR creation was failed
     */
    static void makeJar(Class<?> token, Path sourceDir, Path targetPath) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(targetPath), manifest)) {
            String pathToImplementation = getPathToImplementation(token, "/") +
                    StringConstants.SUFFIX + StringConstants.CLASS_EXTENSION;
            jarOutputStream.putNextEntry(new ZipEntry(pathToImplementation));
            Files.copy(Path.of(sourceDir.toString(), pathToImplementation), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Failure of writing JAR", e);
        }
    }

    /**
     * Returns the path to the token class
     *
     * @param token {@link Class} token
     * @return {@link String} way to class
     * @param separator separator that will separate arguments
     */
    private static String getPathToImplementation(Class<?> token, String separator) {
        return String.join(separator, token.getPackageName().split("\\."))
                + separator + token.getSimpleName();
    }

    /**
     * Finds classPath for token
     *
     * @param token Class token
     * @return classPath for given token
     * @throws ImplerException if generation failed
     */
    private static Path getSuperPath(Class<?> token) throws ImplerException {
        Path superPath;
        try {
            CodeSource codeSource = token.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                throw new ImplerException("Cannot get super class code source");
            }
            URL sourceCodeUrl = codeSource.getLocation();
            if (sourceCodeUrl == null) {
                throw new ImplerException("Cannot get super class code source location");
            }
            String sourceCodePath = sourceCodeUrl.getPath();
            if (sourceCodePath.isEmpty()) {
                throw new ImplerException("Cannot convert code source location");
            }
            if (sourceCodePath.startsWith("/")) {
                sourceCodePath = sourceCodePath.substring(1);
            }
            superPath = Path.of(sourceCodePath);
        } catch (InvalidPathException e) {
            throw new ImplerException("Cannot get super class code source");
        }
        return superPath;
    }
}
