package es.upm.amusementpark.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link InvoiceBuilder} producing invoices rendered as plain text.
 */
public class TextInvoiceBuilder implements InvoiceBuilder {

    private String recipientType;
    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder forRecipient(String recipientType, String recipient) {
        this.recipientType = recipientType;
        this.recipient = recipient;
        this.sections.clear();
        return this;
    }

    @Override
    public InvoiceBuilder addSection(String title, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(title, lines));
        }
        return this;
    }

    @Override
    public Invoice build() {
        if (recipient == null) {
            throw new IllegalStateException("The invoice recipient must be set before building");
        }
        return new Invoice(recipientType, recipient, sections);
    }
}
