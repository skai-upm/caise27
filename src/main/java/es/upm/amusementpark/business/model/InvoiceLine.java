package es.upm.amusementpark.business.model;

import java.math.BigDecimal;

/**
 * One charged item of an invoice.
 */
public record InvoiceLine(String description, BigDecimal amount) {
}
