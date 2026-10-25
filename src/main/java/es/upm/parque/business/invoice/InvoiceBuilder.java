package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Builder that assembles an invoice step by step.
 */
public interface InvoiceBuilder {

    /**
     * Discards any partially built invoice.
     */
    void reset();

    void setRecipient(String recipient);

    void setIssueDate(LocalDate issueDate);

    /**
     * Opens a new section; subsequent items are added to it.
     */
    void startSection(String title);

    void addItem(String description, BigDecimal price);

    /**
     * Returns the invoice built so far, with the total of all items.
     */
    Invoice getResult();
}
