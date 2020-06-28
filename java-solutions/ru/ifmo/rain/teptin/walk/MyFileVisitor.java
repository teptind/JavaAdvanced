package ru.ifmo.rain.teptin.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor extends SimpleFileVisitor<Path> {
    static final int ERROR_HASHCODE = 0;
    private Writer outputWriter;

    MyFileVisitor(Writer writer) {
        outputWriter = writer;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        int hash;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            hash = getFNVHash(bufferedInputStream);
        } catch (IOException | SecurityException e) {
            hash = ERROR_HASHCODE;
        }
        writeHash(path.toString(), hash);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        writeHash(path.toString(), ERROR_HASHCODE);
        return FileVisitResult.CONTINUE;
    }

    void writeHash(String pathString, int hash) throws IOException {
        if (hash == ERROR_HASHCODE) {
            System.err.println("Failure of reading file \"" + pathString + "\"");
        }
        outputWriter.write(String.format("%08x %s%n", hash, pathString));
//        System.out.print(String.format("%08x %s%n", hash, pathString));
        outputWriter.flush();
    }

    private static int getFNVHash(InputStream inputStream) throws IOException {
        int PRIME = 0x01000193;
        int hash = 0x811c9dc5;
        byte[] block = new byte[1024];
        int was_read;
        while ((was_read = inputStream.read(block)) >= 0) {
            for (int i = 0; i < was_read; ++i) {
                hash = (hash * PRIME) ^ Byte.toUnsignedInt(block[i]);
            }
        }
        return hash;
    }
}
