package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * What help shows about one command: its accepted syntaxes and its purpose.
 */
public record CommandInfo(List<String> syntaxes, String description) {

    public CommandInfo {
        syntaxes = List.copyOf(syntaxes);
    }
}
