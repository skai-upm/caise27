package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class InteractiveSessionTest {

    @Test
    void readsAllLinesUntilEndOfInput() {
        InteractiveSession session = new InteractiveSession(new BufferedReader(new StringReader("a\nb\nc\n")));
        assertEquals(3, session.run());
    }

    @Test
    void emptyInputTerminatesImmediately() {
        InteractiveSession session = new InteractiveSession(new BufferedReader(new StringReader("")));
        assertEquals(0, session.run());
    }
}
