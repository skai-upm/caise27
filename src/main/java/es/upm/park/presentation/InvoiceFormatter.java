package es.upm.park.presentation;

import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.invoice.InvoiceItem;
import es.upm.park.business.invoice.InvoiceSection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders invoices as text for the console.
 */
public class InvoiceFormatter {

    private static final String CURRENCY = " EUR";

    public String format(Invoice invoice) {
        List<String> lines = new ArrayList<>();
        lines.add("===== " + invoice.title() + " (" + invoice.issueDate() + ") =====");
        lines.addAll(invoice.recipient());
        for (InvoiceSection section : invoice.sections()) {
            lines.add("-- " + section.title());
            BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
            for (InvoiceItem item : section.items()) {
                lines.add("  " + item.description() + ": " + money(item.amount()));
                for (String detail : item.details()) {
                    lines.add("      " + detail);
                }
                subtotal = subtotal.add(item.amount());
            }
            lines.add("  Subtotal: " + money(subtotal));
        }
        lines.add("TOTAL: " + money(invoice.total()));
        return String.join(System.lineSeparator(), lines);
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + CURRENCY;
    }
}
