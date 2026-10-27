package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * Common abstraction for building invoices step by step. Only the steps relevant to the
 * recipient are invoked; omitted parts simply do not appear in the result.
 */
public interface InvoiceBuilder {

    /** Starts a new invoice for the given recipient type, discarding any previous parts. */
    InvoiceBuilder forRecipient(Invoice.Type type);

    /** Adds an identification line of the recipient. */
    InvoiceBuilder recipient(String label, String value);

    /** Opens a new section; it only appears if at least one item is added to it. */
    InvoiceBuilder section(String title);

    /** Adds a charged item to the current section. */
    InvoiceBuilder item(String description, BigDecimal amount);

    /** Adds a non-charged descriptive line to the last item. */
    InvoiceBuilder detail(String text);

    /** Returns the invoice built so far; its total is the sum of the items shown. */
    Invoice build();
}
