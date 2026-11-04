package com.parque.business.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One charged item of an invoice: a description and its amount.
 */
public class InvoiceLine {
    private final String description;
    private final BigDecimal amount;

    public InvoiceLine(String description, BigDecimal amount) {
        this.description = description;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
