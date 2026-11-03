package es.upm.etsisi.amusementpark.business.invoice;

import es.upm.etsisi.amusementpark.business.model.Invoice;
import es.upm.etsisi.amusementpark.business.model.InvoiceLine;
import es.upm.etsisi.amusementpark.business.model.InvoiceSection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Common state of the concrete builders: the header and the sections added so far. */
abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private final List<InvoiceSection> sections = new ArrayList<>();
    private LocalDate issueDate;

    @Override
    public void buildHeader(LocalDate date) {
        this.issueDate = date;
        sections.clear();
    }

    /** Adds a section only when it has lines, so that inapplicable sections are omitted. */
    protected void addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
    }

    /** Text identifying the recipient on the invoice. */
    protected abstract String recipient();

    @Override
    public Invoice getResult() {
        if (issueDate == null) {
            throw new IllegalStateException("buildHeader must be called first");
        }
        return new Invoice(recipient(), issueDate, sections);
    }
}
