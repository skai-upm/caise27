package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * Builds an invoice step by step. Calling {@link #startInvoice} discards any invoice in progress.
 */
public interface InvoiceBuilder {

    void startInvoice(String title, String recipient);

    void startSection(String title);

    void addLine(String description, BigDecimal amount);

    Invoice build();
}
