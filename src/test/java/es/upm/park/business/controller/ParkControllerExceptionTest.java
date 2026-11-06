package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.exception.StorageException;
import es.upm.park.presentation.CommandProcessor;
import es.upm.park.presentation.CommandRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerExceptionTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.of("ana@correo.com"));
    }

    @AfterEach
    void tearDown() {
        park.reset();
    }

    @Test
    void unknownElementsRaiseNotFound() {
        assertThrows(NotFoundException.class, () -> park.deleteGroup("G99"));
        assertThrows(NotFoundException.class, () -> park.deleteTicket("T99"));
        assertThrows(NotFoundException.class, () -> park.requireAttraction("A99"));
        assertThrows(NotFoundException.class, () -> park.createIndividualTicket("T1", "nadie@correo.com", TODAY));
    }

    @Test
    void ruleViolationsRaiseBusinessRule() {
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        assertThrows(BusinessRuleException.class, () -> park.deleteTicket("T001"));
        assertThrows(BusinessRuleException.class, () -> park.createIndividualTicket("T001", "ana@correo.com", TODAY));
    }

    @Test
    void malformedValuesRaiseInvalidInput() {
        assertThrows(InvalidInputException.class, () -> park.setBasePrice(new BigDecimal("-1")));
        assertThrows(InvalidInputException.class, () -> park.createGroup(" ", "x"));
        assertThrows(InvalidInputException.class, () -> park.createAttraction("A01", "x", 0));
    }

    @Test
    void storageFailureIsReportedAndProcessingContinues() throws IOException {
        park.setStorage(new ParkStorage() {
            @Override
            public ParkState load() {
                return ParkState.empty();
            }

            @Override
            public void save(ParkState state) {
                throw new StorageException("Data could not be saved to groups.txt", new IOException("disk full"));
            }
        });
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new CommandRunner(new CommandProcessor(), new PrintStream(bytes, true, StandardCharsets.UTF_8))
                .run(new BufferedReader(new StringReader("GROUP CREATE;G01;Familia\nHELP\n")));
        String out = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("ERROR: Data could not be saved to groups.txt"));
        assertTrue(out.contains("Available commands:"));
    }
}
