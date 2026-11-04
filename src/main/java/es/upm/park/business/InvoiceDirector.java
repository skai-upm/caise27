package es.upm.park.business;

import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import java.time.LocalDate;
import java.util.List;

/**
 * Drives invoice construction: always the same sequence of steps, whatever the builder.
 */
public class InvoiceDirector {

    public Invoice construct(InvoiceBuilder builder, String recipient, LocalDate date,
                             List<IndividualTicket> directTickets, List<GroupTicket> groupTickets) {
        builder.reset(recipient, date);
        builder.buildIndividualTickets(directTickets);
        builder.buildGroupTickets(groupTickets);
        return builder.getResult();
    }
}
