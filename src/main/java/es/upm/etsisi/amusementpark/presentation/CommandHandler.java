package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.BusinessException;

/**
 * Executes one parsed command; {@code args[0]} is the command name.
 */
@FunctionalInterface
public interface CommandHandler {

    CommandResult handle(String[] args) throws BusinessException, CommandSyntaxException;
}
