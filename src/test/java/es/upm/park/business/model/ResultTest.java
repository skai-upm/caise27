package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void prefixesOkAndError() {
        assertEquals("OK: done", Result.ok("done").toString());
        assertEquals("ERROR: bad", Result.error("bad").toString());
    }

    @Test
    void verbatimIsPrintedUnchanged() {
        Result r = Result.verbatim(true, "OK TICKET CREATED -> 40.00");
        assertTrue(r.success());
        assertFalse(r.terminate());
        assertEquals("OK TICKET CREATED -> 40.00", r.toString());
    }

    @Test
    void exitTerminates() {
        assertTrue(Result.exit("Bye.").terminate());
        assertEquals("OK: Bye.", Result.exit("Bye.").toString());
    }
}
