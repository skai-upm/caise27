package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class UmlModelTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final Path MAIN = Path.of("src/main/java/es/upm/etsisi/amusementpark");

    @Test
    void modelIsPlantUml() throws Exception {
        String uml = Files.readString(MODEL).strip();
        assertTrue(uml.startsWith("@startuml"));
        assertTrue(uml.endsWith("@enduml"));
    }

    @Test
    void everyMainClassAppearsInTheModel() throws IOException {
        String uml = Files.readString(MODEL);
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString().replace(".java", "");
                boolean exception = file.toString().contains("exception");
                boolean helper = name.equals("TextFiles") || name.equals("UserValidator") || name.equals("EntryResult");
                if (!exception && !helper) {
                    assertTrue(uml.contains(name), name + " missing from model.puml");
                }
            }
        }
    }

    @Test
    void deliveryThreeElementsAreRepresented() throws Exception {
        String uml = Files.readString(MODEL);
        for (String fragment : new String[] {
                "abstract class Ticket", "Ticket <|-- IndividualTicket", "Ticket <|-- GroupTicket",
                "GroupTicket \"1\" *--", "IndividualAccess <|.. IndividualTicket", "GroupAccess <|.. GroupTicket",
                "Attraction<T extends GeneralAccess>", "T = IndividualAccess", "T = GroupAccess",
                "SpeedPass \"0..*\" --> \"1\" IndividualTicket", "SpecialPass \"0..1\" --> \"1\" IndividualTicket",
                "InvoiceBuilder <|-- UserInvoiceBuilder", "InvoiceBuilder <|-- GroupInvoiceBuilder",
                "class ParkController <<singleton>>", "class UserController <<singleton>>",
                "class PassController <<singleton>>", "class InvoiceController <<singleton>>", "<<inject>>"}) {
            assertTrue(uml.contains(fragment), fragment);
        }
    }
}
