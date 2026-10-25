package es.upm.parque;

/**
 * Validation of identifiers for groups, tickets and attractions.
 */
public final class Identifiers {

    private Identifiers() {
    }

    /**
     * An identifier is non-empty and contains only letters, digits, '-' or '_',
     * so it can never clash with the separators used in the data files.
     */
    public static boolean isValid(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        for (char c : id.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_') {
                return false;
            }
        }
        return true;
    }

    static void require(String id, String kind) {
        if (!isValid(id)) {
            throw new IllegalArgumentException("Invalid " + kind + " identifier: '" + id
                    + "' (use letters, digits, '-' or '_')");
        }
    }
}
