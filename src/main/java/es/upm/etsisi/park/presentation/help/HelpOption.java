package es.upm.etsisi.park.presentation.help;

/**
 * A help command variant: {@code HELP} for an empty variant, otherwise {@code HELP <variant>}.
 */
public record HelpOption(String variant, String description, HelpGenerator generator) {

    public String commandName() {
        return variant.isEmpty() ? "HELP" : "HELP " + variant;
    }
}
