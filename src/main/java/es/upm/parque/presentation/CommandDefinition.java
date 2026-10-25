package es.upm.parque.presentation;

import java.util.List;

/**
 * A registered command: its name, parameter names (optional ones written as "[Name]"), description and action.
 */
public record CommandDefinition(String name, List<String> parameters, String description, CommandAction action) {

    public int minParameters() {
        int count = 0;
        for (String p : parameters) {
            if (!isOptional(p)) {
                count++;
            }
        }
        return count;
    }

    public int maxParameters() {
        return parameters.size();
    }

    /**
     * Command syntax, e.g. {@code ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]}.
     */
    public String syntax() {
        StringBuilder sb = new StringBuilder(name);
        for (String p : parameters) {
            if (isOptional(p)) {
                sb.append("[;").append(p, 1, p.length() - 1).append(']');
            } else {
                sb.append(';').append(p);
            }
        }
        return sb.toString();
    }

    private static boolean isOptional(String parameter) {
        return parameter.startsWith("[") && parameter.endsWith("]");
    }
}
