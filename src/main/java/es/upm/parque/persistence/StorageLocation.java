package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Data directory used by every repository: the working directory normally, or a fresh
 * timestamped directory under {@code tmp/} in temporary ({@code --fly}) mode.
 */
public final class StorageLocation {

    public static final String FLY = "--fly";

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path directory;
    private final boolean temporary;
    private final String[] commandArgs;

    private StorageLocation(Path directory, boolean temporary, String[] commandArgs) {
        this.directory = directory;
        this.temporary = temporary;
        this.commandArgs = commandArgs;
    }

    /** Chooses the data directory from the program arguments, relative to {@code workDir}. */
    public static StorageLocation fromArgs(String[] args, Path workDir) {
        if (args.length > 0 && FLY.equals(args[0])) {
            String[] rest = Arrays.copyOfRange(args, 1, args.length);
            return new StorageLocation(createTemporaryDirectory(workDir.resolve("tmp")), true, rest);
        }
        return new StorageLocation(workDir, false, args);
    }

    private static Path createTemporaryDirectory(Path tmpRoot) {
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(STAMP);
        try {
            Files.createDirectories(tmpRoot);
            Path candidate = tmpRoot.resolve(stamp);
            for (int suffix = 1; Files.exists(candidate); suffix++) {
                candidate = tmpRoot.resolve(stamp + "_" + suffix);
            }
            return Files.createDirectory(candidate);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot create temporary data directory", e);
        }
    }

    public Path getDirectory() {
        return directory;
    }

    public boolean isTemporary() {
        return temporary;
    }

    /** Program arguments that remain once the storage option has been consumed. */
    public String[] getCommandArgs() {
        return commandArgs.clone();
    }

    /** Deletes the temporary directory and all its files; does nothing in normal mode. */
    public void cleanUp() {
        if (!temporary) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot delete temporary data directory " + directory, e);
        }
    }
}
