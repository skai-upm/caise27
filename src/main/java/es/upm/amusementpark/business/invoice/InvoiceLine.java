package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * One invoiced item: a description and the amount charged for it.
 */
public record InvoiceLine(String description, BigDecimal amount) {
}
