package es.upm.park.presentation.help;

import java.util.Optional;

/**
 * Description of one command: its syntax, a brief purpose and an optional
 * note (such as the accepted values of a field).
 */
public record CommandInfo(String syntax, String purpose, Optional<String> note) {

    public static CommandInfo of(String syntax, String purpose) {
        return new CommandInfo(syntax, purpose, Optional.empty());
    }

    public static CommandInfo of(String syntax, String purpose, String note) {
        return new CommandInfo(syntax, purpose, Optional.of(note));
    }
}
