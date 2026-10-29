package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new UserService();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() throws OperationException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createRegistersUser() {
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertNotNull(service.findByDni("12345678Z"));
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(OperationException.class, () -> service.create("P", "L", "87654321A",
                "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(OperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.es", "Otra2#", "6", "9876543210987654"));
        assertThrows(OperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.com", "otra2#", "6", "9876543210987654"));
        assertThrows(OperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.com", "Otra2#", "6", "98765432109876"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDni() {
        assertThrows(OperationException.class, () -> service.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Otra2#", "6", "9876543210987654"));
        assertNull(service.findByEmail("otra@correo.com"));
    }

    @Test
    void createRejectsDuplicateEmail() {
        assertThrows(OperationException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ana@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void authenticateChecksEmailAndPassword() throws Exception {
        assertSame(service.findByEmail("ana@correo.com"), service.authenticate("ana@correo.com", "Clave1!"));
        assertThrows(OperationException.class, () -> service.authenticate("nadie@correo.com", "Clave1!"));
        assertThrows(OperationException.class, () -> service.authenticate("ana@correo.com", "Mala1!"));
    }

    @Test
    void removeMakesUserInactive() throws Exception {
        createPedro();
        User pedro = service.findByEmail("pedro@correo.com");
        assertTrue(service.isActive(pedro));
        service.remove(pedro);
        assertNull(service.findByEmail("pedro@correo.com"));
        assertFalse(service.isActive(pedro));
    }

    @Test
    void loadReplacesUsers() {
        User luis = new User("Luis", "Ruiz", "00000000T", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        service.load(List.of(luis));
        assertNull(service.findByEmail("ana@correo.com"));
        assertSame(luis, service.findByEmail("luis@correo.com"));
    }

    @Test
    void updateEachSimpleField() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");

        User ana = service.findByEmail("ana@correo.com");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("Nueva2#", ana.getPassword());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateEmailRekeysUser() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertNull(service.findByEmail("ana@correo.com"));
        assertNotNull(service.findByEmail("ana.nueva@correo.com"));
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void updateRejectsWrongCredentials() {
        assertThrows(OperationException.class,
                () -> service.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertEquals("Ana", service.findByEmail("ana@correo.com").getFirstName());
    }

    @Test
    void updateRejectsInvalidValues() {
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() throws Exception {
        createPedro();
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertEquals("Pedro", service.findByEmail("pedro@correo.com").getFirstName());
    }
}
