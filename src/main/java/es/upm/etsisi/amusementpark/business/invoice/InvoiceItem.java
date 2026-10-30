package es.upm.etsisi.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * One priced line of an invoice.
 */
public record InvoiceItem(String description, BigDecimal amount) {
}
