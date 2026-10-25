package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TextFileStoreTest {

    @TempDir
    Path dir;

    @Test
    void missingFileReadsAsEmpty() throws IOException {
        assertTrue(new TextFileStore(dir.resolve("none.txt")).readLines().isEmpty());
    }

    @Test
    void writesAndReadsLinesSkippingBlankOnes() throws IOException {
        TextFileStore store = new TextFileStore(dir.resolve("sub/file.txt"));
        store.writeLines(List.of("a;b", "", "c;d"));
        assertEquals(List.of("a;b", "c;d"), store.readLines());
        store.writeLines(List.of("e"));
        assertEquals(List.of("e"), store.readLines());
    }

    @Test
    void storagePathsKeepEveryFileInOneDirectory() {
        StoragePaths paths = new StoragePaths(dir);
        for (Path file : List.of(paths.users(), paths.groups(), paths.tickets(), paths.groupTickets(),
                paths.attractions())) {
            assertEquals(dir, file.getParent());
        }
    }
}
