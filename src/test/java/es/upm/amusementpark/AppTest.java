package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the application baseline.
 */
class AppTest {

    @Test
    void mainReturnsNormallyAtEndOfStandardInput() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(new byte[0]));
            assertDoesNotThrow(() -> App.main(new String[0]));
        } finally {
            System.setIn(original);
        }
    }
}
