package es.upm.etsisi.park.business.invoice;

import java.math.BigDecimal;

/**
 * One printable line of an invoice section.
 */
public sealed interface InvoiceLine permits ChargeLine, DetailLine {

    String render();

    /** Amount this line adds to the invoice total. */
    BigDecimal charged();
}
