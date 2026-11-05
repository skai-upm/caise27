package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable invoice produced by an {@link InvoiceBuilder}.
 */
public final class Invoice {

    private final String title;
    private final List<String> recipient;
    private final Map<String, List<InvoiceLine>> sections;
    private final BigDecimal total;

    Invoice(String title, List<String> recipient, Map<String, List<InvoiceLine>> sections, BigDecimal total) {
        this.title = title;
        this.recipient = List.copyOf(recipient);
        this.sections = Collections.unmodifiableMap(new LinkedHashMap<>(sections));
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public Map<String, List<InvoiceLine>> getSections() {
        return sections;
    }

    public BigDecimal getTotal() {
        return total;
    }

    /** Legible text form of the invoice. */
    public String render() {
        StringBuilder sb = new StringBuilder(title);
        String nl = System.lineSeparator();
        recipient.forEach(line -> sb.append(nl).append(line));
        sections.forEach((name, lines) -> {
            sb.append(nl).append("[").append(name).append("]");
            for (InvoiceLine l : lines) {
                sb.append(nl).append(l.counted() ? "  " : "      ").append(l.description());
                if (l.amount() != null) {
                    sb.append(" : ").append(money(l.amount()));
                }
            }
        });
        return sb.append(nl).append("TOTAL: ").append(money(total)).toString();
    }

    static String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " EUR";
    }
}
