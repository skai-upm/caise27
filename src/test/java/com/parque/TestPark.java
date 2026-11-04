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
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

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

    /** Passwords used by the tests. */
    public static final List<String> TEST_PASSWORDS = List.of("Clave1!", "Otra2#", "Nueva2#", "Luis3$", "Marta4%");

    /**
     * Replaces the BCrypt hash in the password field (5th) of user-file lines by the test password it verifies,
     * so expected lines can be written with plaintext while proving that only a matching hash was stored.
     */
    public static List<String> unhash(List<String> userLines) {
        List<String> result = new ArrayList<>();
        for (String line : userLines) {
            String[] f = line.split(";", -1);
            if (f.length == 7) {
                String plain = "<not a hash of a test password>";
                if (f[4].startsWith("$2")) {
                    for (String candidate : TEST_PASSWORDS) {
                        if (BCrypt.checkpw(candidate, f[4])) {
                            plain = candidate;
                        }
                    }
                }
                f[4] = plain;
                line = String.join(";", f);
            }
            result.add(line);
        }
        return result;
    }
}
