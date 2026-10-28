package es.upm.etsisi.amusementpark.business.model;

/**
 * A user registered in the park. Users do not hold references to their tickets.
 * The DNI cannot change after creation; the password is kept only in protected form.
 */
public class User {

    private String firstName;
    private String surnames;
    private final String dni;
    private String email;
    private String passwordHash;
    private String phone;
    private String card;

    public User(String dni, String email) {
        this(dni, email, null);
    }

    /**
     * @param passwordHash protected (BCrypt) value of the password, or {@code null} if none is stored
     */
    public User(String dni, String email, String passwordHash) {
        this("", "", dni, email, passwordHash, "", "");
    }

    /**
     * @param passwordHash protected (BCrypt) value of the password, or {@code null} if none is stored
     */
    public User(String firstName, String surnames, String dni, String email, String passwordHash, String phone,
            String card) {
        this.firstName = firstName;
        this.surnames = surnames;
        this.dni = dni;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.card = card;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurnames() {
        return surnames;
    }

    public void setSurnames(String surnames) {
        this.surnames = surnames;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /** Protected value of the password; the original password is never kept. */
    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean hasPassword() {
        return passwordHash != null;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }
}
