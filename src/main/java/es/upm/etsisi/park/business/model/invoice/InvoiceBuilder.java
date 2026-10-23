package es.upm.etsisi.park.business.model.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.business.model.ticket.Money;

/**
 * Common abstraction of the invoice builders. An invoice is assembled step
 * by step — header, recipient lines and any number of sections — and then
 * obtained with {@link #build()}. Sections that end up without lines are
 * left out, and the total is the sum of the priced items.
 * <p>
 * Concrete builders add the steps that make sense for their recipient type.
 */
public abstract class InvoiceBuilder {

    private String title;
    private LocalDate date;
    private List<String> recipient;
    private List<SectionDraft> sections;

    protected InvoiceBuilder() {
        reset();
    }

    /** Discards everything built so far. */
    public final void reset() {
        title = "INVOICE";
        date = null;
        recipient = new ArrayList<>();
        sections = new ArrayList<>();
    }

    /** First step: invoice title and issue date. */
    public InvoiceBuilder header(String invoiceTitle, LocalDate issueDate) {
        this.title = invoiceTitle;
        this.date = issueDate;
        return this;
    }

    protected void recipientLine(String line) {
        recipient.add(line);
    }

    /** Opens a new section; following items and details go into it. */
    protected void section(String sectionTitle) {
        sections.add(new SectionDraft(sectionTitle));
    }

    /** A priced line counting towards the total. */
    protected void item(String description, BigDecimal amount) {
        currentSection().lines.add(InvoiceLine.item(description, Money.of(amount)));
    }

    /** A descriptive line that does not count towards the total. */
    protected void detail(String description) {
        currentSection().lines.add(InvoiceLine.detail(description));
    }

    /** Final step: the invoice with its non-empty sections. */
    public Invoice build() {
        if (date == null) {
            throw new IllegalStateException("The invoice header has not been built");
        }
        List<InvoiceSection> built = new ArrayList<>();
        for (SectionDraft draft : sections) {
            if (!draft.lines.isEmpty()) {
                built.add(new InvoiceSection(draft.title, draft.lines));
            }
        }
        Invoice invoice = new Invoice(title, date, recipient, built);
        reset();
        return invoice;
    }

    private SectionDraft currentSection() {
        if (sections.isEmpty()) {
            throw new IllegalStateException("No section has been opened");
        }
        return sections.get(sections.size() - 1);
    }

    private static final class SectionDraft {
        private final String title;
        private final List<InvoiceLine> lines = new ArrayList<>();

        private SectionDraft(String title) {
            this.title = title;
        }
    }
}
