package es.upm.parque.business.invoice;

import java.math.BigDecimal;

/**
 * One invoiced item with its price.
 */
public record InvoiceLine(String description, BigDecimal price) {
}
