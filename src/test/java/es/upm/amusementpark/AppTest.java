package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

/**
 * Unit test for the application baseline.
 */
class AppTest {

    @Test
    void mainRunsWithoutErrors() {
        assertDoesNotThrow(() -> App.main(new String[0]));
    }
}
