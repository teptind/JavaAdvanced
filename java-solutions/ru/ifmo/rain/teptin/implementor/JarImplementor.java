package ru.ifmo.rain.teptin.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

// java -cp . -p . -m info.kgeorgiy.java.advanced.implementor interface ru.ifmo.teptin.implementor.JarImplementor
// java -cp . -p . -m info.kgeorgiy.java.advanced.implementor jar-interface ru.ifmo.rain.teptin.implementor.JarImplementor

/**
 * Implementor of jar and .java files by interface
 * @author Daniil Teptin
 */

public class JarImplementor implements Impler, JarImpler {
    /** Default Constructor.
     *  No default behaviour as the class has no fields
     */
    public JarImplementor() {}
    /**
     * Validates given token whether the class type is supported
     * @param token - token to validate
     * @throws ImplerException whether validation was incorrect
     */
    private void validateToken(Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();
        if (!token.isInterface() || Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("This class type is unsupported");
        }
    }

    /**
     * Constructs path to
     *
     * @param token {@link Class} class token
     * @param root {@link Path} path do directory
     * @return {@link Path} path to created directory
     * @throws ImplerException whether creation was failed
     */
    private Path getPathForGeneratedSource(Class<?> token, Path root) throws ImplerException {
        String resultPathString = String.join(File.separator, token.getPackageName().split("\\."))
                + (File.separator + token.getSimpleName() + StringConstants.SUFFIX + StringConstants.JAVA_EXTENSION);
        Path resultPath;
        try {
            resultPath = Paths.get(root.toString(), resultPathString);
        } catch (InvalidPathException e) {
            throw new ImplerException("Cannot generate a correct path");
        }
        Path resultPathParent = resultPath.getParent();
        if (resultPathParent != null) {
            try {
                Files.createDirectories(resultPathParent);
            } catch (IOException e) {
                throw new ImplerException("Cannot create parent directories for file with generated source");
            }
        }
        return resultPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Not null arguments are required");
        }
        validateToken(token);
        Path generatedSourcePath = getPathForGeneratedSource(token, root);
        try (BufferedWriter generatedSourceWriter = Files.newBufferedWriter(generatedSourcePath)) {
            generatedSourceWriter.write(toRightEncoding(SourceCreator.generateSource(token)));
        } catch (IOException e) {
            throw new ImplerException("Failure with writing down the source");
        }
    }

    /**
     * Constructs temporary path to jarFile that will be made
     * @param path {@link Path} path do directory
     * @return {@link Path} path to created directory
     * @throws ImplerException whether creation was failed
     */

    private Path getTempPathForJar(Path path) throws ImplerException {
        String message = "Cannot create parent directories for jar-file with generated source";
        Path pathParentDir = path.toAbsolutePath().normalize().getParent();
        if (pathParentDir != null) {
            try {
                Files.createDirectories(pathParentDir);
            } catch (IOException e) {
                throw new ImplerException(message);
            }
        } else {
            pathParentDir = Path.of("");
        }
        Path pathTempDir;
        try {
            pathTempDir = Files.createTempDirectory(pathParentDir, "jar-temp-dir");
        } catch (IOException e) {
            throw new ImplerException(message);
        }
        return pathTempDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Not null arguments are required");
        }
        validateToken(token);
        Path tempSourceDir = getTempPathForJar(jarFile);
        try {
            implement(token, tempSourceDir);
            JarUtils.compile(token, tempSourceDir);
            JarUtils.makeJar(token, tempSourceDir, jarFile);
        } catch (ImplerException e) {
            throw new ImplerException("Implementation failure", e);
        } finally {
            deleteDirectory(tempSourceDir.toFile());
        }
    }

    /**
     * Recursive delete directories and files
     *
     * @param fileToDelete - root directory, will be deleted last
     */
    private void deleteDirectory(File fileToDelete) {
        File[] allContents = fileToDelete.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        fileToDelete.delete();
    }

    /**
     * Gets string in a valid encoding
     * @param s {@link String}
     * @return {@link String}
     */
    private static String toRightEncoding(String s) {
        StringBuilder res = new StringBuilder();
        char[] data = s.toCharArray();
        for (char c : data) {
            if (c < 128) {
                res.append(c);
            } else {
                res.append("\\u").append(String.format("%04x", (int) c));
//                16bit format not less than 4 digits with adding zeroes
            }
        }
        return res.toString();
    }

    /**
     * Main method
     *
     * When <code>-jar</code> is omitted, the program runs in Implementation mode and
     * {@link #implement(Class, Path)} is invoked.
     * When <code>-jar</code> is used, the program runs in JarImplementation mode and
     * {@link #implementJar(Class, Path)} is invoked.
     * <p>
     * All arguments must not be null.
     * All errors will go to <code>STDOUT</code>
     * <p>
     * Usage: ([-jar]) [ClassName] [Path]
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        String USAGE = "USAGE: ([-jar]) [ClassName] [Path]";
        //  remake
        if (args == null || !(args.length == 2 || args.length == 3)) {
            System.out.println(USAGE);
            return;
        }
        if (Arrays.asList(args).contains(null)) {
            System.out.println("Not null arguments are required");
            return;
        }

        boolean jarEnabled;
        if (args.length == 2) {
            jarEnabled = false;
        } else {
            if (args[0].equals("-jar")) {
                jarEnabled = true;
                args[0] = args[1];
                args[1] = args[2];
            } else {
                System.out.println(USAGE);
                return;
            }
        }

        Class<?> classInfo;
        try {
            classInfo = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class is not found");
            return;
        }

        Path root;
        try {
            root = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Error: Invalid parent directory");
            return;
        }

        JarImplementor implementor = new JarImplementor();

        try {
            if (jarEnabled) {
                implementor.implementJar(classInfo, root);
            } else {
                implementor.implement(classInfo, root);
            }
        } catch (ImplerException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
