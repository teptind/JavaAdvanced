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

// java -cp . -p . -m info.kgeorgiy.java.advanced.implementor interface ru.ifmo.teptin.implementor.Implementor
// java -cp . -p . -m info.kgeorgiy.java.advanced.implementor jar-interface ru.ifmo.rain.teptin.implementor.Implementor

public class Implementor implements Impler, JarImpler {
    private void validateToken(Class<?> token) throws ImplerException {
        int modifiers = token.getModifiers();
        if (!token.isInterface() || Modifier.isFinal(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new ImplerException("This class type is unsupported");
        }
    }

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

    private void deleteDirectory(File fileToDelete) {
        File[] allContents = fileToDelete.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        fileToDelete.delete();
    }

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

        Implementor implementor = new Implementor();

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
