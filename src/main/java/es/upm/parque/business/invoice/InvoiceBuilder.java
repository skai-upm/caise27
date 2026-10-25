package es.upm.parque.business.invoice;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

import java.util.List;

/**
 * Builds an invoice step by step. Each section step may be skipped; empty sections are not added.
 */
public interface InvoiceBuilder {

    InvoiceBuilder start(String recipient);

    InvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets);

    InvoiceBuilder addPasses(List<Pass> passes);

    InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets);

    Invoice build();
}
