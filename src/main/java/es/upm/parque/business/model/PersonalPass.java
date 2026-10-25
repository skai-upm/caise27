package es.upm.parque.business.model;

import java.math.BigDecimal;

/**
 * A pass bought on top of an individual ticket. Consumed passes are kept with their status.
 */
public abstract class PersonalPass {

    private final String id;
    private final BigDecimal price;
    private boolean consumed;

    protected PersonalPass(String id, BigDecimal price, boolean consumed) {
        this.id = id;
        this.price = price;
        this.consumed = consumed;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

    /**
     * Short kind name used in texts and storage.
     */
    public abstract String getKind();
}
