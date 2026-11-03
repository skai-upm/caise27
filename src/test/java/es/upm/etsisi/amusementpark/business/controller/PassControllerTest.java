package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.persistence.PassRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassControllerTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    /** Repository whose save can be made to fail. */
    static class FlakyRepository implements PassRepository {
        boolean fail;
        List<Pass> saved = new ArrayList<>();

        @Override
        public List<Pass> load(Function<String, Optional<Ticket>> ticketById) {
            return new ArrayList<>();
        }

        @Override
        public void save(List<Pass> passes) {
            if (fail) {
                throw new IllegalStateException("disk full");
            }
            saved = new ArrayList<>(passes);
        }
    }

    FlakyRepository repository;
    PassController passes;

    @BeforeEach
    void setUp() {
        App.createProcessor(dir).process(TestData.CREATE_ANA);
        TicketController tickets = TicketController.getInstance();
        tickets.create("T001", "ana@correo.com", TODAY.plusDays(14), TODAY, ParkController.DEFAULT_PRICE);
        repository = new FlakyRepository();
        passes = PassController.initialize(repository, tickets);
    }

    @Test
    void failedSaveRevertsEveryOperation() {
        passes.buySpeedPasses("S1", "T001", 1, TODAY);
        repository.fail = true;
        assertThrows(IllegalStateException.class, () -> passes.buySpeedPasses("S2", "T001", 10, TODAY));
        assertThrows(IllegalStateException.class, () -> passes.buySpecialPass("SP1", "T001", TODAY));
        assertThrows(IllegalStateException.class, () -> passes.refund(SpeedPass.TYPE, "S1-1", TODAY));
        assertEquals(List.of("S1-1"), passes.getPasses().stream().map(Pass::getId).toList());
    }

    @Test
    void passesOfATicket() {
        passes.buySpeedPasses("S1", "T001", 10, TODAY);
        Ticket t = TicketController.getInstance().find("T001").orElseThrow();
        assertEquals(10, passes.passesOf(t).size());
        assertEquals(10, repository.saved.size());
        assertTrue(passes.find("S1-11").isEmpty());
    }

    @Test
    void initializeRegistersTheUniqueInstance() {
        assertSame(passes, PassController.getInstance());
    }

    @Test
    void failedConsumptionIsUndone() {
        List<Pass> bought = passes.buySpeedPasses("S1", "T001", 1, TODAY);
        repository.fail = true;
        assertThrows(IllegalStateException.class, () -> passes.consume(bought));
        assertTrue(bought.stream().noneMatch(Pass::isConsumed));
        passes.consume(List.of());
    }

    @Test
    void failedEntryLeavesNobodyInsideAndNoPassConsumed() {
        passes.buySpeedPasses("S1", "T001", 1, TODAY.plusDays(14));
        ParkController park = ParkController.initialize(UserController.getInstance(), GroupController.getInstance(),
                TicketController.getInstance(), AttractionController.getInstance(), passes, TODAY.plusDays(14));
        park.attractions().create("A01", "Tornado", "5");
        repository.fail = true;
        assertThrows(IllegalStateException.class, () -> park.enter("A01", "T001", true));
        assertTrue(park.attractions().get("A01").getPresent().isEmpty());
        assertTrue(passes.getPasses().stream().noneMatch(Pass::isConsumed));
        repository.fail = false;
        EntryResult result = park.enter("A01", "T001", true);
        assertEquals(1, result.getAdmitted());
        assertEquals("S1-1", result.getConsumed().get(0).getId());
    }
}
