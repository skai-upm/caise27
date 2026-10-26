package es.upm.parque.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder of text invoices. Sections without lines are left out and the total is the sum of all lines.
 */
public class TextInvoiceBuilder implements InvoiceBuilder {

    private String title;
    private String recipient;
    private final List<InvoiceSection> sections = new ArrayList<>();
    private String sectionTitle;
    private final List<InvoiceLine> sectionLines = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    @Override
    public void startInvoice(String title, String recipient) {
        this.title = title;
        this.recipient = recipient;
        sections.clear();
        sectionTitle = null;
        sectionLines.clear();
        total = BigDecimal.ZERO;
    }

    @Override
    public void startSection(String title) {
        closeSection();
        sectionTitle = title;
    }

    @Override
    public void addLine(String description, BigDecimal amount) {
        if (sectionTitle == null) {
            throw new IllegalStateException("startSection must be called before addLine");
        }
        sectionLines.add(new InvoiceLine(description, amount));
        total = total.add(amount);
    }

    @Override
    public Invoice build() {
        if (title == null) {
            throw new IllegalStateException("startInvoice must be called before build");
        }
        closeSection();
        return new Invoice(title, recipient, sections, total.setScale(2, RoundingMode.HALF_UP));
    }

    private void closeSection() {
        if (sectionTitle != null && !sectionLines.isEmpty()) {
            sections.add(new InvoiceSection(sectionTitle, sectionLines));
        }
        sectionTitle = null;
        sectionLines.clear();
    }
}
