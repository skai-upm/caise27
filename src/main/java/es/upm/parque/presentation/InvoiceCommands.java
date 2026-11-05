package es.upm.parque.presentation;

import es.upm.parque.business.InvoiceManager;
import es.upm.parque.business.NotFoundException;
import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceItem;
import es.upm.parque.business.model.InvoiceSection;

/**
 * Parses the INVOICE CREATE command and renders the invoice built by {@link InvoiceManager} as text.
 */
class InvoiceCommands {

    static final String INVOICE_CREATE = "INVOICE CREATE";
    private static final String USER = "USER";
    private static final String GROUP = "GROUP";
    private static final String USAGE = "INVOICE CREATE expects: INVOICE CREATE;USER;<email> or "
            + "INVOICE CREATE;GROUP;<groupIdentifier>";
    private static final String EUR = " EUR";
    private static final String NL = System.lineSeparator();

    private final InvoiceManager invoices;

    InvoiceCommands(InvoiceManager invoices) {
        this.invoices = invoices;
    }

    String create(String[] e) throws InvalidCommandException, NotFoundException {
        CommandProcessor.expect(e, 3, USAGE);
        if (e[2].isEmpty()) {
            throw new InvalidCommandException(USAGE);
        }
        Invoice invoice = switch (e[1]) {
            case USER -> invoices.forUser(e[2]);
            case GROUP -> invoices.forGroup(e[2]);
            default -> throw new InvalidCommandException(USAGE);
        };
        return render(invoice);
    }

    static String render(Invoice invoice) {
        StringBuilder text = new StringBuilder("OK: ").append(invoice.getTitle());
        invoice.getRecipient().forEach(line -> text.append(NL).append("  ").append(line));
        for (InvoiceSection section : invoice.getSections()) {
            text.append(NL).append("  ").append(section.getHeading()).append(':');
            for (InvoiceItem item : section.getItems()) {
                text.append(NL).append("    ").append(item.getDescription()).append("  ")
                        .append(item.getAmount().toPlainString()).append(EUR);
                item.getDetails().forEach(d -> text.append(NL).append("      - ").append(d));
            }
        }
        return text.append(NL).append("  TOTAL: ").append(invoice.getTotal().toPlainString()).append(EUR).toString();
    }
}
