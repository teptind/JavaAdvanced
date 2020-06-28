package ru.ifmo.rain.teptin.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Walk <input file> <output file>");
            return;
        }
        try {
            walk(args[0], args[1]);
        } catch (WalkException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void walk(String inputFileName, String outputFileName) throws WalkException {
        createOutput(outputFileName);
        try (BufferedReader inputFileReader = Files.newBufferedReader(Paths.get(inputFileName))) {
            try (BufferedWriter outputFileWriter = Files.newBufferedWriter(Paths.get(outputFileName))) {
                MyFileVisitor hashingFileVisitor = new MyFileVisitor(outputFileWriter);
                String currentPath;
                do {
                    try {
                        currentPath = inputFileReader.readLine();
                    } catch (IOException e) {
                        throw new WalkException("Failure of reading input file \"" + inputFileName + "\"", e);
                    }
                    if (currentPath != null) {
                        try {
                            try {
//                                hashingFileVisitor.visitFile(Paths.get(currentPath), );
                                Files.walkFileTree(Paths.get(currentPath), hashingFileVisitor);
                            } catch (InvalidPathException e) {
                                hashingFileVisitor.writeHash(currentPath, MyFileVisitor.ERROR_HASHCODE);
                            }
                        } catch (IOException e) {
                            throw new WalkException("Failure of writing to output file \"" + outputFileName, e);
                        }
                    }
                } while (currentPath != null);
            } catch (SecurityException | IOException e) {
                throw new WalkException("Unable to open output file \"" + outputFileName + "\" to write", e);
            }
        } catch (SecurityException | InvalidPathException | IOException e) {
            throw new WalkException("Unable to open input file \"" + inputFileName + "\"", e);
        }
    }

    private static void createOutput(String outputFileName) throws WalkException {
        Path outputFilePath;
        try {
            outputFilePath = Paths.get(outputFileName);
            Path outputFilePathParent = outputFilePath.getParent();
            if (outputFilePathParent != null) {
                Files.createDirectories(outputFilePathParent);
            }
        } catch (InvalidPathException | IOException e) {
            throw new WalkException("Unable to create output file \'" + outputFileName + "\'", e);
        }
    }
}
