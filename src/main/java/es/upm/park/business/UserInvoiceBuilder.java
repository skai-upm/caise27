package es.upm.park.business;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import java.util.List;

/**
 * Builds user invoices: only the individual tickets bought directly by the user are charged.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    @Override
    public void buildIndividualTickets(List<IndividualTicket> tickets) {
        addSection("Individual tickets", tickets.stream()
                .map(t -> new Invoice.Line(t.getId() + " " + t.getDate(), t.getPrice()))
                .toList());
    }
}
