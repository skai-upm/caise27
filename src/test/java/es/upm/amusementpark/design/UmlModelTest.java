package es.upm.amusementpark.design;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml stays consistent with the implemented model.
 */
class UmlModelTest {

    private static List<String> lines;

    @BeforeAll
    static void read() throws IOException {
        lines = Files.readAllLines(Path.of("uml/model.puml")).stream().map(String::trim).toList();
    }

    private static boolean declares(String name) {
        for (String kind : List.of("class ", "abstract class ", "enum ", "interface ")) {
            for (String l : lines) {
                if (l.startsWith(kind + name)) {
                    String rest = l.substring((kind + name).length());
                    if (rest.isEmpty() || rest.startsWith(" ") || rest.startsWith("<") || rest.startsWith("{")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Test
    void everyModelClassAndParkIsDeclared() throws IOException {
        try (Stream<Path> files = Files.list(Path.of("src/main/java/es/upm/amusementpark/business/model"))) {
            for (Path f : files.toList()) {
                String name = f.getFileName().toString().replace(".java", "");
                assertTrue(declares(name), name + " missing from uml/model.puml");
            }
        }
        assertTrue(declares("Park"));
    }

    @Test
    void keyRelationshipsAndDirections() {
        for (String rel : List.of("Ticket <|-- IndividualTicket", "Ticket <|-- GroupTicket",
                "GroupTicket *-- \"1..*\" IndividualTicket", "IndividualTicket --> \"1\" User",
                "GroupTicket --> \"0..1\" Group", "Group --> \"0..*\" User", "Attraction --> \"0..*\" User")) {
            assertTrue(lines.stream().anyMatch(l -> l.startsWith(rel)), rel + " missing");
        }
        for (String rel : List.of("SpeedPass \"0..*\" --> \"1\" IndividualTicket", "SpecialPass \"0..1\" --> \"1\" IndividualTicket",
                "IndividualAccess <|.. IndividualTicket", "GroupAccess <|.. GroupTicket", "InvoiceBuilder <|-- UserInvoiceBuilder",
                "InvoiceBuilder <|-- GroupInvoiceBuilder", "ApplicationInitializer ..> UserService : injects")) {
            assertTrue(lines.stream().anyMatch(l -> l.startsWith(rel)), rel + " missing");
        }
        assertTrue(lines.contains("abstract class Attraction<T extends TicketAccess> {"));
        for (String singleton : List.of("UserService", "GroupService", "TicketService", "AttractionService", "PassService",
                "InvoiceService", "Park")) {
            assertTrue(lines.stream().anyMatch(l -> l.startsWith("class " + singleton + " <<singleton>>")), singleton);
        }
        assertTrue(lines.stream().noneMatch(l -> l.startsWith("User -") || l.startsWith("User *") || l.startsWith("User o")),
                "User must not reference other elements");
    }
}
