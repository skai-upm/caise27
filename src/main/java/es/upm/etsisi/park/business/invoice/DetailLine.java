package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;

/**
 * Informative line (for example a group-ticket constituent) that adds nothing to the total.
 */
public record DetailLine(String text) implements InvoiceLine {

    @Override
    public String render() {
        return "    - " + text;
    }

    @Override
    public BigDecimal charged() {
        return BigDecimal.ZERO;
    }
}
