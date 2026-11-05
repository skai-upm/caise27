package es.upm.parque.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.business.model.Invoice;
import es.upm.parque.business.model.InvoiceItem;

class InvoiceBuilderTest {

    /** Records the order in which the director invokes the construction steps. */
    private static final class RecordingBuilder extends InvoiceBuilder {
        private final List<String> steps = new ArrayList<>();

        RecordingBuilder() {
            super("Test invoice");
        }

        @Override
        public void buildRecipient() {
            steps.add("recipient");
            addRecipientLine("Someone");
        }

        @Override
        public void buildPurchases() {
            steps.add("purchases");
            addSection("Empty", List.of());
            addSection("Bought", List.of(new InvoiceItem("thing", new BigDecimal("12.5"), List.of("detail"))));
        }

        @Override
        public void buildPersonalPasses() {
            steps.add("passes");
        }
    }

    @Test
    void directorRunsTheStepsInOrderAndEmptySectionsAreOmitted() {
        RecordingBuilder builder = new RecordingBuilder();
        Invoice invoice = InvoiceManager.construct(builder);
        assertEquals(List.of("recipient", "purchases", "passes"), builder.steps);
        assertEquals("Test invoice", invoice.getTitle());
        assertEquals(List.of("Someone"), invoice.getRecipient());
        assertEquals(1, invoice.getSections().size());
        assertEquals("Bought", invoice.getSections().get(0).getHeading());
        assertEquals(new BigDecimal("12.50"), invoice.getTotal());
    }

    @Test
    void defaultPersonalPassesStepAddsNothing() {
        InvoiceBuilder builder = new InvoiceBuilder("Plain") {
            @Override
            public void buildRecipient() {
                addRecipientLine("x");
            }

            @Override
            public void buildPurchases() {
                // no purchases
            }
        };
        Invoice invoice = InvoiceManager.construct(builder);
        assertTrue(invoice.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), invoice.getTotal());
    }
}
