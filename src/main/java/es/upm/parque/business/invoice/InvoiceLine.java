package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * One charged item of an invoice.
 *
 * @param description what was bought
 * @param amount      price charged for it
 */
public record InvoiceLine(String description, BigDecimal amount) {
}
