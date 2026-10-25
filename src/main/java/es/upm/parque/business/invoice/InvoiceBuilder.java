package es.upm.parque.business.invoice;

import java.time.LocalDate;
import java.util.List;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

/**
 * Builds an invoice step by step. Each section step may be skipped by the director, and builders leave out
 * sections without items.
 */
public interface InvoiceBuilder {

    void reset();

    void buildHeader(String recipientType, String recipient, LocalDate date);

    void buildIndividualTickets(List<IndividualTicket> tickets);

    void buildPasses(List<Pass> passes);

    void buildGroupTickets(List<GroupTicket> groupTickets);

    Invoice getResult();
}
