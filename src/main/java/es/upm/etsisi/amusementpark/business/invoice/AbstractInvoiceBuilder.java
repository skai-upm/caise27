package es.upm.etsisi.amusementpark.business.invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared builder steps: header lines and sections, omitting sections with no lines.
 */
public abstract class AbstractInvoiceBuilder implements InvoiceBuilder {

    private String title;
    private final List<String> header = new ArrayList<>();
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public void reset(String invoiceTitle) {
        title = invoiceTitle;
        header.clear();
        sections.clear();
    }

    @Override
    public void addHeaderLine(String line) {
        header.add(line);
    }

    protected void addSection(String sectionTitle, List<InvoiceLine> lines) {
        if (!lines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, lines));
        }
    }

    @Override
    public Invoice build() {
        if (title == null) {
            throw new IllegalStateException("reset must be called before build");
        }
        return new Invoice(title, header, sections);
    }
}
