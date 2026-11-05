package es.upm.amusementpark.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryTwoIntegrationTest {

    @TempDir
    Path dir;

    static List<String> scenario() {
        String today = LocalDate.now().toString();
        String future = LocalDate.now().plusDays(30).toString();
        return List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "SYSTEM PRICE;45.50",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;" + today,
                "GROUPTICKET CREATE;TG01;G01;" + today,
                "GROUPTICKET CREATE;TG02;G01;" + future,
                "GROUPTICKET DELETE;TG02",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION EXIT;A01;ana@correo.com",
                "ATTRACTION ENTER;A01;T001",
                "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01",
                "HELP",
                "EXIT");
    }

    private String run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeliveryOneIntegrationTest.runApp(args, "", out, new ByteArrayOutputStream(), dir.resolve("users.txt"));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sameFileInFlyAndNormalMode() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.write(input, scenario(), StandardCharsets.UTF_8);
        String fly = run("--fly", input.toString());
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
        assertFalse(Files.exists(dir.resolve("users.txt")));
        String normal = run(input.toString());
        assertEquals(fly, normal);
        assertFalse(normal.contains("ERROR"), normal);
        assertTrue(normal.contains("GROUP TICKET CREATED. Total price: 91.00 EUR"));
        assertTrue(normal.contains("Inside: 2"));
        for (String f : List.of("users.txt", "groups.txt", "tickets.txt", "attractions.txt")) {
            assertTrue(Files.exists(dir.resolve(f)), f);
        }
    }

    @Test
    void usersAndGroupsDoNotReferenceTicketsOrAttractions() {
        for (Class<?> type : List.of(User.class, Group.class)) {
            for (Field f : type.getDeclaredFields()) {
                assertFalse(Ticket.class.isAssignableFrom(f.getType()) || f.getType() == Attraction.class,
                        type.getSimpleName() + "." + f.getName());
                if (Collection.class.isAssignableFrom(f.getType())) {
                    String generic = f.getGenericType().getTypeName();
                    assertFalse(generic.contains("Ticket") || generic.contains("Attraction"), generic);
                }
            }
        }
    }
}
