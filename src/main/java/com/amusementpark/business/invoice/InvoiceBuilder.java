package com.amusementpark.business.invoice;

import com.amusementpark.business.model.GroupTicket;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.SpecialPass;
import com.amusementpark.business.model.SpeedPass;

import java.util.List;

/**
 * Builds an invoice step by step. Each section step adds its section only when there are items.
 */
public interface InvoiceBuilder {

    InvoiceBuilder forRecipient(String recipient);

    InvoiceBuilder addIndividualTickets(List<IndividualTicket> tickets);

    InvoiceBuilder addGroupTickets(List<GroupTicket> groupTickets);

    InvoiceBuilder addSpeedPasses(List<SpeedPass> speedPasses);

    InvoiceBuilder addSpecialPasses(List<SpecialPass> specialPasses);

    Invoice build();
}
