package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * One billed item of an invoice.
 */
public record InvoiceLine(String description, BigDecimal amount) {
}
