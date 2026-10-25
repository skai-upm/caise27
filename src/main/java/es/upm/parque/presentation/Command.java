package es.upm.parque.presentation;

import es.upm.parque.presentation.help.CommandInfo;

/**
 * A registered command.
 *
 * @param name        operation, e.g. {@code USER CREATE}
 * @param params      parameter syntax, optional ones in brackets, e.g. {@code Id;Nombre[;Tipo]}
 * @param description what the command does
 * @param minParams   minimum number of parameters
 * @param maxParams   maximum number of parameters
 * @param handler     behavior
 */
public record Command(String name, String params, String description, int minParams, int maxParams,
                      CommandHandler handler) {

    public String syntax() {
        return params.isEmpty() ? name : name + ";" + params;
    }

    public CommandInfo info() {
        return new CommandInfo(syntax(), description);
    }
}
