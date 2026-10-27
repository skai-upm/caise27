package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.exception.ParkException;

/**
 * SYSTEM DATE / PRICE commands.
 */
class SystemCommands {

    private static final String DATE_ERROR = "Invalid date. Use SYSTEM DATE;<yyyy-MM-dd>";
    private static final String PRICE_ERROR = "Invalid price. Use a positive amount such as SYSTEM PRICE;45.50";

    private final ParkController park;

    SystemCommands(ParkController park) {
        this.park = park;
    }

    String date(CommandInput in) throws InvalidCommandException {
        in.expect(1, "SYSTEM DATE;<yyyy-MM-dd>");
        park.setCurrentDate(in.date(1, DATE_ERROR));
        return "SYSTEM DATE UPDATED";
    }

    String price(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "SYSTEM PRICE;<Price>");
        park.setCurrentPrice(in.amount(1, PRICE_ERROR));
        return "SYSTEM PRICE UPDATED";
    }
}
