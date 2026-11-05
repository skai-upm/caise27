package es.upm.park.business;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Invoice;
import es.upm.park.business.model.Pass;
import java.util.List;

/**
 * Builds user invoices: the individual tickets bought directly by the user and the user's
 * personal passes are charged; tickets inside group tickets are not.
 */
public class UserInvoiceBuilder extends InvoiceBuilder {

    @Override
    public void buildIndividualTickets(List<IndividualTicket> tickets) {
        addSection("Individual tickets", tickets.stream()
                .map(t -> new Invoice.Line(t.getId() + " " + t.getDate(), t.getPrice()))
                .toList());
    }

    @Override
    public void buildPasses(List<Pass> passes) {
        addSection("Personal passes", passes.stream()
                .map(p -> new Invoice.Line(p.getId() + " " + p.kind() + " (ticket " + p.getTicket().getId() + ")",
                        p.getPrice()))
                .toList());
    }
}
