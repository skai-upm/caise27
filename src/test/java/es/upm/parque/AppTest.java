package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the application entry point.
 */
public class AppTest {

    @Test
    public void entryPointStartsWithoutArguments() {
        assertDoesNotThrow(() -> App.main(new String[0]));
    }
}
