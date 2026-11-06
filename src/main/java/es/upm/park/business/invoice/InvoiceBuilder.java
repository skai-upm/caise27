package es.upm.park.business.invoice;

import java.math.BigDecimal;

/**
 * Common abstraction for building invoices step by step. A director calls
 * only the steps relevant to the invoice it is producing.
 */
public interface InvoiceBuilder {

    /** Starts a new invoice of the given type, discarding previous steps. */
    InvoiceBuilder reset(String type);

    /** Adds a line identifying the recipient. */
    InvoiceBuilder recipient(String line);

    /** Opens a section; following items and details belong to it. */
    InvoiceBuilder section(String title);

    /** Adds a priced item to the current section. */
    InvoiceBuilder item(String description, BigDecimal amount);

    /** Adds a non-priced detail line to the current section. */
    InvoiceBuilder detail(String text);

    /** Returns the invoice built from the steps applied so far. */
    Invoice build();
}
