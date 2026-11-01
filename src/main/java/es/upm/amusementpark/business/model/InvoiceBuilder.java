package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * Builds an {@link Invoice} step by step: recipient, then any number of sections
 * with their items, then {@link #build()}.
 */
public interface InvoiceBuilder {

    InvoiceBuilder recipient(String kind, String id, String name);

    /**
     * Starts a new section; subsequent items belong to it.
     */
    InvoiceBuilder section(String title);

    InvoiceBuilder item(String description, BigDecimal amount);

    /**
     * Returns the invoice, omitting sections without items and computing the total.
     */
    Invoice build();
}
