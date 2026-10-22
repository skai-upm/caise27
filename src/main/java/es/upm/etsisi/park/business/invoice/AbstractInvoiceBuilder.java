package es.upm.etsisi.park.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Construction steps shared by every invoice: title, recipient data and sections. A section
 * without lines is omitted.
 *
 * @param <B> the concrete builder, returned by the fluent steps
 */
public abstract class AbstractInvoiceBuilder<B extends AbstractInvoiceBuilder<B>> implements InvoiceBuilder {

    private String title = "INVOICE";
    private final List<String> recipient = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    /** The concrete builder ({@code this}), for fluent chaining. */
    protected abstract B self();

    protected B title(String text) {
        this.title = text;
        return self();
    }

    protected B recipientLine(String line) {
        recipient.add(line);
        return self();
    }

    protected B section(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
        return self();
    }

    @Override
    public Invoice build() {
        return new Invoice(title, recipient, sections);
    }
}
