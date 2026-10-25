package es.upm.parque;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.persistence.TextTicketRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TicketTest extends ParkTestSupport {

    @BeforeEach
    void anaAndDate() {
        createAna();
        ok("SYSTEM DATE;2026-10-01");
    }

    private Ticket ticket(String id) {
        return tickets().find(id);
    }

    @Test
    public void buysTicketForRegisteredUser() {
        assertEquals("OK TICKET CREATED -> 40.00", run("TICKET CREATE;T001;" + ANA + ";2026-10-15"));
        IndividualTicket t001 = assertInstanceOf(IndividualTicket.class, ticket("T001"));
        assertSame(user(ANA), t001.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), t001.getDate());
        assertEquals("40.00", t001.getPrice().toPlainString());
    }

    @Test
    public void ticketForTodayIsAllowed() {
        assertEquals("OK TICKET CREATED -> 40.00", run("TICKET CREATE;T001;" + ANA + ";2026-10-01"));
    }

    @Test
    public void rejectsInvalidPurchases() {
        fails("TICKET CREATE;T002;nadie@correo.com;2026-10-15");
        fails("TICKET CREATE;T003;" + ANA + ";2026-09-30");
        fails("TICKET CREATE;T004;" + ANA + ";15-10-2026");
        fails("TICKET CREATE;;" + ANA + ";2026-10-15");
        fails("TICKET CREATE;T,5;" + ANA + ";2026-10-15");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        fails("TICKET CREATE;T001;" + ANA + ";2026-10-20");
        assertEquals(1, tickets().getIndividualTickets().size());
    }

    @Test
    public void deletesOnlyFutureTickets() {
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SYSTEM DATE;2026-10-15");
        fails("TICKET DELETE;T001");
        ok("SYSTEM DATE;2026-10-16");
        fails("TICKET DELETE;T001");
        ok("SYSTEM DATE;2026-10-14");
        assertEquals("OK TICKET DELETED", run("TICKET DELETE;T001"));
        assertNull(ticket("T001"));
        fails("TICKET DELETE;T001");
    }

    @Test
    public void activeTicketBlocksUserDeletionUntilPast() {
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        fails("USER DELETE;" + ANA + ";Clave1!");
        ok("SYSTEM DATE;2026-10-15");
        fails("USER DELETE;" + ANA + ";Clave1!");
        ok("SYSTEM DATE;2026-10-16");
        ok("USER DELETE;" + ANA + ";Clave1!");
        assertNull(user(ANA));
        assertNotNull(ticket("T001"), "past tickets are kept as history");
    }

    @Test
    public void ticketsSurviveRestartWithTheirOwnPrice() throws Exception {
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SYSTEM PRICE;60.00");
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"),
                Files.readAllLines(dataDir.resolve(TextTicketRepository.TICKETS_FILE_NAME)));
        restart();
        IndividualTicket t001 = assertInstanceOf(IndividualTicket.class, ticket("T001"));
        assertSame(user(ANA), t001.getUser());
        assertEquals("40.00", t001.getPrice().toPlainString());
    }

    @Test
    public void historyOfDeletedUserIsRestored() {
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SYSTEM DATE;2026-10-16");
        ok("USER DELETE;" + ANA + ";Clave1!");
        restart();
        IndividualTicket t001 = assertInstanceOf(IndividualTicket.class, ticket("T001"));
        assertEquals("12345678Z", t001.getUser().getDni());
        assertNull(user(ANA));
    }

    @Test
    public void malformedTicketLinesAreSkipped() throws Exception {
        Files.write(dataDir.resolve(TextTicketRepository.TICKETS_FILE_NAME),
                List.of("T001;12345678Z;2026-10-15;40.00", "T002;12345678Z;mal;40.00", "T003;corto"));
        Files.write(dataDir.resolve(TextTicketRepository.GROUP_TICKETS_FILE_NAME), List.of("TG;G;2026-13-01;1;T001"));
        restart();
        assertNotNull(ticket("T001"));
        assertNull(ticket("T002"));
        assertNull(ticket("TG"));
    }
}
