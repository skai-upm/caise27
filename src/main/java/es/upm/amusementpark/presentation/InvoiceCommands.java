package es.upm.amusementpark.presentation;

import java.util.Map;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceController;
import es.upm.amusementpark.business.invoice.InvoiceItem;
import es.upm.amusementpark.business.invoice.InvoiceSection;

/**
 * INVOICE CREATE command and the textual presentation of invoices.
 */
class InvoiceCommands {

    private static final String SYNTAX = "INVOICE CREATE;USER;<email> or INVOICE CREATE;GROUP;<groupIdentifier>";

    private final InvoiceController invoices;

    InvoiceCommands(InvoiceController invoices) {
        this.invoices = invoices;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(2, SYNTAX);
        Invoice invoice = switch (in.get(1)) {
            case "USER" -> invoices.userInvoice(in.get(2));
            case "GROUP" -> invoices.groupInvoice(in.get(2));
            default -> throw new InvalidCommandException("Invalid format. Use " + SYNTAX);
        };
        return render(invoice);
    }

    static String render(Invoice invoice) {
        StringBuilder out = new StringBuilder("INVOICE (" + invoice.getType() + ")");
        for (Map.Entry<String, String> line : invoice.getRecipient()) {
            out.append('\n').append(line.getKey()).append(": ").append(line.getValue());
        }
        for (InvoiceSection section : invoice.getSections()) {
            out.append("\n[").append(section.title()).append(']');
            for (InvoiceItem item : section.items()) {
                out.append("\n  ").append(item.description()).append(": ")
                        .append(item.amount().toPlainString()).append(" EUR");
                for (String detail : item.details()) {
                    out.append("\n    - ").append(detail);
                }
            }
        }
        return out.append("\nTOTAL: ").append(invoice.getTotal().toPlainString()).append(" EUR").toString();
    }
}
