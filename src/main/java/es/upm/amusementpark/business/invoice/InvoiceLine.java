package es.upm.amusementpark.business.invoice;

import java.math.BigDecimal;

/** One charged item of an invoice. */
public record InvoiceLine(String description, BigDecimal amount) {
}
