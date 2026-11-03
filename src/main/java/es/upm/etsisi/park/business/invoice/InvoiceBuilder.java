package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;

/**
 * Common abstraction for building invoices step by step.
 */
public interface InvoiceBuilder {

    InvoiceBuilder title(String title);

    InvoiceBuilder headerLine(String line);

    /** Starts a section; sections that receive no lines are left out of the invoice. */
    InvoiceBuilder section(String title);

    /** Adds a priced item to the current section and to the total. */
    InvoiceBuilder item(String text, BigDecimal amount);

    /** Adds an informative line that is not counted in the total. */
    InvoiceBuilder detail(String text);

    Invoice build();
}
