package com.parque;

import com.parque.business.controller.ParkController;
import com.parque.business.service.AttractionService;
import com.parque.business.service.GroupService;
import com.parque.business.service.InvoiceDirector;
import com.parque.business.service.TicketService;
import com.parque.business.service.UserService;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Test helper: starts a fresh execution (new unique controller) for each test.
 */
public final class TestPark {
    private TestPark() {
    }

    /** Ends any previous execution and wires a new one over {@code dataDir}, like the application does. */
    public static ParkController start(Path dataDir) throws IOException {
        ApplicationContext.stop();
        return ApplicationContext.start(dataDir);
    }

    /** Ends any previous execution and initializes the controller with the given services (OS date). */
    public static ParkController controller(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions) {
        ParkController.release();
        return ParkController.initialize(users, groups, tickets, attractions, new InvoiceDirector());
    }

    /** As {@link #controller(UserService, GroupService, TicketService, AttractionService)} starting at {@code date}. */
    public static ParkController controller(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions, LocalDate date) {
        ParkController.release();
        return ParkController.initialize(users, groups, tickets, attractions, new InvoiceDirector(), date);
    }
}
