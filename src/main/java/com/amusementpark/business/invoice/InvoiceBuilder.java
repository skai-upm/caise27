package com.amusementpark.business.invoice;

import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds an invoice step by step. Steps given an empty list add no section.
 */
public interface InvoiceBuilder {

    InvoiceBuilder start(String recipient, LocalDate date);

    InvoiceBuilder addTickets(List<IndividualTicket> tickets);

    InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets);

    Invoice build();
}
