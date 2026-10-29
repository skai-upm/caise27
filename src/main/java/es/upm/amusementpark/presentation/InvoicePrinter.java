package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceLine;
import es.upm.amusementpark.business.invoice.InvoiceSection;

import java.io.PrintStream;
import java.math.BigDecimal;

/**
 * Renders an invoice as readable text.
 */
public class InvoicePrinter {

    public void print(Invoice invoice, PrintStream out) {
        out.println(invoice.getTitle());
        for (String line : invoice.getHeader()) {
            out.println(line);
        }
        for (InvoiceSection section : invoice.getSections()) {
            out.println("-- " + section.title() + " --");
            for (InvoiceLine line : section.lines()) {
                out.println("  " + line.description() + ": " + money(line.amount()));
                for (String detail : line.details()) {
                    out.println("      " + detail);
                }
            }
        }
        out.println("TOTAL: " + money(invoice.getTotal()));
    }

    static String money(BigDecimal amount) {
        return amount.toPlainString() + " EUR";
    }
}
