package es.upm.etsisi.park.business.invoice;

import es.upm.etsisi.park.business.Money;

import java.math.BigDecimal;

/**
 * A charged item: contributes its amount to the invoice total.
 */
public record ChargeLine(String description, BigDecimal amount) implements InvoiceLine {

    @Override
    public String render() {
        return "  " + description + " ... " + Money.format(amount);
    }

    @Override
    public BigDecimal charged() {
        return amount;
    }
}
