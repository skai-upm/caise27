package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SystemDateTest {

    @Test
    void startsWithOperatingSystemDate() {
        assertEquals(LocalDate.now(), new SystemDate().get());
    }

    @Test
    void sharedInstanceSeesChanges() {
        SystemDate date = new SystemDate();
        SystemDate sameReference = date;
        date.set(LocalDate.of(2026, 10, 1));
        assertEquals(LocalDate.of(2026, 10, 1), sameReference.get());
    }
}
