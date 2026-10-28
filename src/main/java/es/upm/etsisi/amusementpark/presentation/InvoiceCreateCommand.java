package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;

/**
 * INVOICE CREATE;USER;&lt;email&gt; and INVOICE CREATE;GROUP;&lt;groupIdentifier&gt;.
 */
public class InvoiceCreateCommand extends ControllerCommand<InvoiceController> {

    public InvoiceCreateCommand(InvoiceController controller) {
        super("INVOICE CREATE;<USER|GROUP>;<email|groupIdentifier>", 2, controller);
    }

    @Override
    protected String run(InvoiceController controller, String[] fields) {
        Invoice invoice = switch (fields[0]) {
            case "USER" -> controller.createUserInvoice(fields[1]);
            case "GROUP" -> controller.createGroupInvoice(fields[1]);
            default -> throw new InvalidValueException("Invalid invoice type, expected USER or GROUP: " + fields[0]);
        };
        return InvoiceFormatter.format(invoice);
    }
}
