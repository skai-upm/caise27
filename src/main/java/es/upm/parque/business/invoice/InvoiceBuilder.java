package es.upm.parque.business.invoice;

import java.time.LocalDate;
import java.util.List;

/**
 * Builder of invoices: an invoice is assembled in distinct steps and obtained at the end with
 * {@link #getResult()}.
 */
public interface InvoiceBuilder {

    /** Discards any partially built invoice. */
    void reset();

    void buildHeader(String recipientType, String recipientId, String recipientName, LocalDate issueDate);

    void buildSection(String title, List<InvoiceLine> lines);

    Invoice getResult();
}
