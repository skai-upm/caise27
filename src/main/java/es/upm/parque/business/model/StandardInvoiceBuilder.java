package es.upm.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Default invoice builder: keeps the steps in order, drops empty sections and sums the lines.
 */
public class StandardInvoiceBuilder implements InvoiceBuilder {

    private String recipientKind;
    private String recipientId;
    private String recipientName;
    private LocalDate issueDate;
    private final List<InvoiceSection> sections = new ArrayList<>();

    @Override
    public InvoiceBuilder recipient(String kind, String id, String name) {
        this.recipientKind = kind;
        this.recipientId = id;
        this.recipientName = name;
        return this;
    }

    @Override
    public InvoiceBuilder issuedOn(LocalDate date) {
        this.issueDate = date;
        return this;
    }

    @Override
    public InvoiceBuilder section(String title) {
        sections.add(new InvoiceSection(title));
        return this;
    }

    @Override
    public InvoiceBuilder line(String description, BigDecimal amount) {
        if (sections.isEmpty()) {
            throw new IllegalStateException("A line needs a section");
        }
        sections.get(sections.size() - 1).addLine(new InvoiceLine(description, amount));
        return this;
    }

    @Override
    public Invoice build() {
        List<InvoiceSection> filled = sections.stream().filter(s -> !s.isEmpty()).toList();
        BigDecimal total = filled.stream()
                .flatMap(s -> s.getLines().stream())
                .map(InvoiceLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new Invoice(recipientKind, recipientId, recipientName, issueDate, filled, total);
    }
}
