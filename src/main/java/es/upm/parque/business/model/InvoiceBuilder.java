package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Builder abstraction that assembles an {@link Invoice} in distinct steps.
 */
public interface InvoiceBuilder {

    InvoiceBuilder recipient(String kind, String id, String name);

    InvoiceBuilder issuedOn(LocalDate date);

    /**
     * Starts a new section; following lines belong to it. Sections left without lines are omitted.
     */
    InvoiceBuilder section(String title);

    InvoiceBuilder line(String description, BigDecimal amount);

    /**
     * Returns the invoice assembled so far, with its total computed from the lines.
     */
    Invoice build();
}
