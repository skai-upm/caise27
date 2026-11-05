package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Immutable invoice produced by an {@link InvoiceBuilder}. Not persisted.
 *
 * @param type recipient type, for example {@code USER} or {@code GROUP}
 * @param recipient identifying information of the recipient (label to value), in insertion order
 * @param sections non-empty sections in order of first use
 * @param total sum of the counted items
 */
public record Invoice(String type, Map<String, String> recipient, List<Section> sections, BigDecimal total) {

    /**
     * Invoice section, for example "Individual tickets".
     *
     * @param title section title
     * @param lines section lines
     */
    public record Section(String title, List<Line> lines) {
    }

    /**
     * Invoice line. Counted items carry an amount; detail lines do not and never add to the total.
     *
     * @param text description
     * @param amount amount, or {@code null} for an informational detail
     */
    public record Line(String text, BigDecimal amount) {

        public boolean isItem() {
            return amount != null;
        }
    }
}
