package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * Builds an invoice in steps: the recipient, then each section followed by its items, then the
 * final invoice. Each step returns the builder so steps can be chained.
 */
public interface InvoiceBuilder {

    InvoiceBuilder recipient(String recipient);

    /**
     * Starts a new section; following items are added to it.
     */
    InvoiceBuilder section(String title);

    InvoiceBuilder item(String description, BigDecimal amount);

    /**
     * Returns the invoice assembled so far and prepares the builder for a new one.
     */
    Invoice build();
}
