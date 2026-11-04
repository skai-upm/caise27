package com.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class HelpGeneratorTest {
    final CommandCatalog catalog = new CommandCatalog(List.of(new CommandInfo("HELP", "Lists commands."),
            new CommandInfo("EXIT", "Ends the execution.")));

    @Test
    void syntaxFormat() {
        assertEquals(List.of("Available commands:", "  HELP", "  EXIT"),
                new HelpGenerator(catalog, new SyntaxHelpFormatter()).generate().lines().toList());
    }

    @Test
    void extendedFormat() {
        assertEquals(List.of("Available commands (extended):", "  HELP", "      Lists commands.", "  EXIT",
                "      Ends the execution."),
                new HelpGenerator(catalog, new ExtendedHelpFormatter()).generate().lines().toList());
    }

    @Test
    void injectedAlternativeBehaviourIsUsed() {
        HelpFormatter csv = commands -> String.join(",", commands.stream().map(CommandInfo::syntax).toList());
        assertEquals("HELP,EXIT", new HelpGenerator(catalog, csv).generate());
    }

    @Test
    void everyStandardCommandHasSyntaxAndDescription() {
        for (CommandInfo c : CommandCatalog.standard().commands()) {
            assertEquals(false, c.syntax().isBlank());
            assertEquals(false, c.description().isBlank(), c.syntax());
        }
    }
}
