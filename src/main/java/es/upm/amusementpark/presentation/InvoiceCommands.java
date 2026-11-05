package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.invoice.Invoice;
import java.io.PrintStream;
import java.util.Map;

/**
 * {@code INVOICE CREATE;USER;<email>} and {@code INVOICE CREATE;GROUP;<groupIdentifier>}.
 */
public final class InvoiceCommands {

    private static final String USAGE = "INVOICE CREATE;USER;<email> | INVOICE CREATE;GROUP;<groupIdentifier>";

    private InvoiceCommands() {
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        processor.register("INVOICE CREATE", line -> {
            String[] f = CommandSupport.fields(line, 3, USAGE);
            Invoice invoice = switch (f[1]) {
                case "USER" -> park.createUserInvoice(f[2]);
                case "GROUP" -> park.createGroupInvoice(f[2]);
                default -> throw new InvalidCommandException("Invalid invoice type. Usage: " + USAGE);
            };
            print(invoice, out);
            return true;
        });
    }

    /** Writes a legible textual representation of the invoice. */
    static void print(Invoice invoice, PrintStream out) {
        out.println("INVOICE - " + invoice.type());
        for (Map.Entry<String, String> entry : invoice.recipient().entrySet()) {
            out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        for (Invoice.Section section : invoice.sections()) {
            out.println(section.title().toUpperCase());
            for (Invoice.Line line : section.lines()) {
                if (line.isItem()) {
                    out.println("  " + line.text() + "  " + line.amount().toPlainString());
                } else {
                    out.println("    - " + line.text());
                }
            }
        }
        out.println("TOTAL: " + invoice.total().toPlainString());
    }
}
