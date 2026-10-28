package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Test helper for users files: password columns hold BCrypt values, which differ on every run.
 */
final class StoredUsers {

    private StoredUsers() {
    }

    /**
     * Checks that every password column is a BCrypt value and replaces it with {@code <bcrypt>}.
     */
    static List<String> masked(List<String> lines) {
        return lines.stream().map(line -> {
            String[] f = line.split(";", -1);
            assertTrue(f[4].startsWith("$2a$"), "password must be stored as BCrypt: " + line);
            f[4] = "<bcrypt>";
            return String.join(";", f);
        }).toList();
    }
}
