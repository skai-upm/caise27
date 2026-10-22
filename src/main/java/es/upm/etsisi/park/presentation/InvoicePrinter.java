package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.invoice.Invoice;
import es.upm.etsisi.park.business.invoice.InvoiceLine;
import es.upm.etsisi.park.business.invoice.InvoiceSection;

import java.io.PrintStream;
import java.math.BigDecimal;

/**
 * Renders an invoice as text.
 */
public class InvoicePrinter {

    private static final String CURRENCY = " EUR";
    private static final String RULE = "----------------------------------------";

    private final PrintStream out;

    public InvoicePrinter(PrintStream out) {
        this.out = out;
    }

    public void print(Invoice invoice) {
        out.println(RULE);
        out.println(invoice.getTitle());
        for (String line : invoice.getRecipient()) {
            out.println(line);
        }
        for (InvoiceSection section : invoice.getSections()) {
            out.println();
            out.println(section.title() + ":");
            for (InvoiceLine line : section.lines()) {
                out.println("  " + line.description() + "  " + money(line.amount()));
                for (String detail : line.details()) {
                    out.println("      " + detail);
                }
            }
        }
        out.println();
        out.println("TOTAL: " + money(invoice.getTotal()));
        out.println(RULE);
    }

    static String money(BigDecimal amount) {
        return amount.toPlainString() + CURRENCY;
    }
}
