package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class UmlModelTest {

    private static final Path MODEL = Path.of("uml", "model.puml");

    @Test
    void modelExistsAndMentionsEveryDomainType() throws Exception {
        String uml = Files.readString(MODEL);
        assertTrue(uml.strip().startsWith("@startuml"));
        assertTrue(uml.strip().endsWith("@enduml"));
        for (Class<?> type : List.of(ParkController.class, User.class, UserField.class, Group.class, Ticket.class,
                IndividualTicket.class, GroupTicket.class, GroupDiscount.class, Attraction.class)) {
            assertTrue(uml.contains(type.getSimpleName()), type.getSimpleName() + " missing from model.puml");
        }
        assertTrue(uml.contains("abstract class Ticket"));
        assertTrue(uml.contains("Ticket <|-- IndividualTicket"));
        assertTrue(uml.contains("Ticket <|-- GroupTicket"));
        assertTrue(uml.contains("GroupTicket \"1\" *--"));
    }
}
