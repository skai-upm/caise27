package es.upm.park.business.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class TextInvoiceBuilderTest {

    @Test
    void buildsOnlyNonEmptySectionsAndSumsPricedItems() {
        InvoiceBuilder b = new TextInvoiceBuilder();
        Invoice inv = b.reset("GROUP").recipient("Group G01 - F")
                .section("Empty")
                .section("Group tickets").item("TG01", new BigDecimal("108.00")).detail("TG01:1 a 36.00")
                .build();
        assertEquals("GROUP", inv.getType());
        assertEquals(List.of("Group G01 - F"), inv.getRecipient());
        assertEquals(1, inv.getSections().size());
        assertEquals(new BigDecimal("108.00"), inv.getTotal());
        assertTrue(inv.getSections().get(0).lines().get(0).isPriced());
        assertFalse(inv.getSections().get(0).lines().get(1).isPriced());
        String nl = System.lineSeparator();
        assertEquals("INVOICE (GROUP)" + nl + "Group G01 - F" + nl + "Group tickets:" + nl + "  TG01  108.00" + nl
                + "      TG01:1 a 36.00" + nl + "TOTAL: 108.00", inv.render());
    }

    @Test
    void resetStartsAFreshInvoiceAndLinesNeedASection() {
        InvoiceBuilder b = new TextInvoiceBuilder();
        b.reset("USER").recipient("Ana").section("S").item("x", BigDecimal.ONE);
        Invoice fresh = b.reset("USER").recipient("Pedro").build();
        assertEquals(List.of("Pedro"), fresh.getRecipient());
        assertTrue(fresh.getSections().isEmpty());
        assertEquals(new BigDecimal("0.00"), fresh.getTotal());
        assertThrows(IllegalStateException.class, () -> new TextInvoiceBuilder().item("x", BigDecimal.ONE));
        assertThrows(IllegalStateException.class, () -> new TextInvoiceBuilder().detail("x"));
    }
}
