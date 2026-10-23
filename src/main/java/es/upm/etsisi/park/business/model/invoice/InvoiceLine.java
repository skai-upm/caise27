package es.upm.etsisi.park.business.model.invoice;

import java.math.BigDecimal;

/**
 * One invoice line. Items carry an amount that counts towards the total;
 * details ({@code amount == null}) only describe an item, e.g. the
 * composition of a group ticket.
 */
public record InvoiceLine(String description, BigDecimal amount) {

    public static InvoiceLine item(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount);
    }

    public static InvoiceLine detail(String description) {
        return new InvoiceLine(description, null);
    }

    public boolean isItem() {
        return amount != null;
    }
}
