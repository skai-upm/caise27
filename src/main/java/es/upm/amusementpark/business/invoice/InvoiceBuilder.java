package es.upm.amusementpark.business.invoice;

import java.time.LocalDate;
import java.util.List;

/**
 * Common abstraction for building invoices step by step. Concrete builders add
 * the steps that make sense for their kind of recipient.
 *
 * @param <B> the concrete builder, so that steps can be chained
 */
public interface InvoiceBuilder<B extends InvoiceBuilder<B>> {

    /** Sets the issue date. */
    B issuedOn(LocalDate date);

    /** Adds a section with {@code items}; an empty list adds nothing. */
    B section(String title, List<InvoiceItem> items);

    /** Returns the invoice built from the steps applied so far. */
    Invoice build();
}
