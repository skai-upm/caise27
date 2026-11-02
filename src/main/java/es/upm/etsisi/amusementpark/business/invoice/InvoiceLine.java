package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * One invoice line. Charged lines add to the total; detail lines only describe a charged item
 * (for example the composition of a group ticket) and are not counted again.
 */
public record InvoiceLine(String description, BigDecimal amount, boolean charged) {

    public static InvoiceLine charge(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount, true);
    }

    public static InvoiceLine detail(String description, BigDecimal amount) {
        return new InvoiceLine(description, amount, false);
    }
}
