package es.upm.parque.presentation.help;

/**
 * A registered command: its syntax (operation followed by ';'-separated parameter names) and a
 * brief description of its behaviour.
 *
 * @param syntax      usage, e.g. {@code USER DELETE;Correo;Contraseña}
 * @param description what the command does
 */
public record CommandInfo(String syntax, String description) {

    /**
     * The operation name, i.e. the text before the first parameter.
     */
    public String operation() {
        return syntax.split(";", -1)[0];
    }

    public int parameterCount() {
        return syntax.split(";", -1).length - 1;
    }
}
