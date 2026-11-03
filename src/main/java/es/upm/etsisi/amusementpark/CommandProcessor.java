package es.upm.etsisi.amusementpark;

/**
 * Interprets API-like text commands, split with {@link String#split}, and returns a one-line response
 * starting with {@code OK:} or {@code ERROR:}. Never throws for invalid input.
 *
 * <pre>
 * USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
 * USER DELETE;Correo;Contraseña
 * USER UPDATE;Correo;Contraseña;Campo;NuevoValor
 * </pre>
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final String WORD_SEPARATOR = " ";
    private static final int CREATE_TOKENS = 8;
    private static final int DELETE_TOKENS = 3;
    private static final int UPDATE_TOKENS = 5;

    private final UserService users;

    public CommandProcessor(UserService users) {
        this.users = users;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return error("Empty command");
        }
        String[] tokens = line.split(PARAM_SEPARATOR, -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        String[] words = tokens[0].split(WORD_SEPARATOR);
        if (words.length != 2 || !words[0].equalsIgnoreCase("USER")) {
            return error("Unknown command: " + tokens[0]);
        }
        try {
            return switch (words[1].toUpperCase(java.util.Locale.ROOT)) {
                case "CREATE" -> create(tokens);
                case "DELETE" -> delete(tokens);
                case "UPDATE" -> update(tokens);
                default -> error("Unknown command: " + tokens[0]);
            };
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Operation failed: " + e.getMessage());
        }
    }

    private String create(String[] t) {
        if (t.length != CREATE_TOKENS) {
            return usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        User u = users.create(t[1], t[2], t[3], t[4], t[5], t[6], t[7]);
        return "OK: User " + u.getEmail() + " created";
    }

    private String delete(String[] t) {
        if (t.length != DELETE_TOKENS) {
            return usage("USER DELETE;Correo;Contraseña");
        }
        users.delete(t[1], t[2]);
        return "OK: User " + t[1] + " deleted";
    }

    private String update(String[] t) {
        if (t.length != UPDATE_TOKENS) {
            return usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field = UserField.parse(t[3]);
        if (field == null) {
            return error("Field " + t[3] + " cannot be updated (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        users.update(t[1], t[2], field, t[4]);
        return "OK: User " + t[1] + " updated (" + field + ")";
    }

    private static String usage(String format) {
        return error("Wrong number of parameters. Usage: " + format);
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
