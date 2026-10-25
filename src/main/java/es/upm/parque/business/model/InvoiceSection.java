package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A titled group of invoice lines.
 */
public class InvoiceSection {

    private final String title;
    private final List<InvoiceLine> lines = new ArrayList<>();

    public InvoiceSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<InvoiceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    void addLine(InvoiceLine line) {
        lines.add(line);
    }

    boolean isEmpty() {
        return lines.isEmpty();
    }
}
