package es.upm.parque.business.model;

/**
 * Kinds of personal pass.
 */
public enum PassKind {
    SPEED("SpeedPass"),
    SPECIAL("Special Pass");

    private final String displayName;

    PassKind(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
