package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExecutionOptionsTest {

    @Test
    void parsesFlyFlagAndInputFile() {
        assertEquals(new ExecutionOptions(false, null), ExecutionOptions.parse(new String[0]));
        assertEquals(new ExecutionOptions(false, "in.txt"), ExecutionOptions.parse(new String[] {"in.txt"}));
        assertEquals(new ExecutionOptions(true, null), ExecutionOptions.parse(new String[] {"--fly"}));
        assertEquals(new ExecutionOptions(true, "in.txt"), ExecutionOptions.parse(new String[] {"--fly", "in.txt"}));
        assertEquals(new ExecutionOptions(true, "in.txt"), ExecutionOptions.parse(new String[] {"in.txt", "--fly"}));
        assertEquals(new ExecutionOptions(false, "a.txt"), ExecutionOptions.parse(new String[] {"a.txt", "b.txt"}));
    }
}
