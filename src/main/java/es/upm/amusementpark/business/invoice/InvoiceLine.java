package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;

/**
 * One billed item: a description and its amount.
 */
public record InvoiceLine(String description, BigDecimal amount) {
}
