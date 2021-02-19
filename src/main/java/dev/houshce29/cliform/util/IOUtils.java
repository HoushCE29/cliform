package dev.houshce29.cliform.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class IOUtils {
    private IOUtils() {
    }

    public static List<String> readResource(String resource) {
        InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resource);
        if (input == null) {
            return Collections.emptyList();
        }
        return readInputStream(input);
    }

    public static List<String> readFile(String path) {
        try {
            return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static List<String> readInputStream(InputStream input) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            return reader.lines()
                    .collect(Collectors.toList());
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
