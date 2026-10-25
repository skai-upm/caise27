package es.upm.parque;

import es.upm.parque.business.UserException;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserControllerTest extends ParkTestSupport {

    private UserController manager() {
        return users();
    }

    private void createAnaDirect() {
        manager().create("Ana", "Garcia Lopez", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedroDirect() {
        manager().create("Pedro", "Lopez Martin", "87654321X", PEDRO, "Otra2#", "623456789", "9876543210987654");
    }

    @Test
    public void createsValidUserAndPersistsIt() {
        createAnaDirect();
        assertEquals(1, manager().getUsers().size());
        restart();
        User ana = manager().findByEmail(ANA);
        assertNotNull(ana);
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    public void rejectsInvalidFieldsOnCreate() {
        UserController m = manager();
        assertThrows(UserException.class, () -> m.create("", "Garcia", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", " ", "12345678Z", ANA, "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", "Garcia", "12345678A", ANA, "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", "Garcia", "12345678Z", "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", "Garcia", "12345678Z", ANA, "clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", "Garcia", "12345678Z", ANA, "Clave1!", "", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Ana", "Garcia", "12345678Z", ANA, "Clave1!", "612345678", "123456789012345"));
        assertTrue(m.getUsers().isEmpty());
        restart();
        assertTrue(manager().getUsers().isEmpty());
    }

    @Test
    public void rejectsDuplicateDniOrEmail() {
        createAnaDirect();
        UserController m = manager();
        assertThrows(UserException.class, () -> m.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Otra", "Persona", "87654321X", ANA, "Clave1!", "600000000", "1234567890123456"));
        assertThrows(UserException.class, () -> m.create("Otra", "Persona", "87654321X", "ANA@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, m.getUsers().size());
    }

    @Test
    public void uniquenessHoldsAfterRestart() {
        createAnaDirect();
        restart();
        UserController m = manager();
        assertThrows(UserException.class, () -> m.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
    }

    @Test
    public void lowercaseDniLetterIsNormalized() {
        manager().create("Ana", "Garcia Lopez", "12345678z", ANA, "Clave1!", "612345678", "1234567890123456");
        assertEquals("12345678Z", manager().findByEmail(ANA).getDni());
    }

    @Test
    public void deletesWithCorrectCredentials() {
        createAnaDirect();
        manager().delete(ANA, "Clave1!");
        assertTrue(manager().getUsers().isEmpty());
        restart();
        assertTrue(manager().getUsers().isEmpty());
    }

    @Test
    public void doesNotDeleteWithWrongPasswordOrUnknownEmail() {
        createAnaDirect();
        UserController m = manager();
        assertThrows(UserException.class, () -> m.delete(ANA, "Otra1!"));
        assertThrows(UserException.class, () -> m.delete("nadie@correo.com", "Clave1!"));
        restart();
        assertEquals(1, manager().getUsers().size());
    }

    @Test
    public void updatesEveryEditableField() {
        createAnaDirect();
        UserController m = manager();
        m.update(ANA, "Clave1!", UserField.NAME, "Ana Maria");
        m.update(ANA, "Clave1!", UserField.SURNAME, "Garcia");
        m.update(ANA, "Clave1!", UserField.PHONE, "699999999");
        m.update(ANA, "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        m.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        m.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");

        restart();
        User ana = manager().findByDni("12345678Z");
        assertEquals("Ana Maria", ana.getName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertDoesNotThrow(() -> manager().authenticate("ana.nueva@correo.com", "Nueva2#"));
        assertEquals("9999888877776666", ana.getCard());
        assertNull(manager().findByEmail(ANA));
    }

    @Test
    public void updateRevalidatesNewValues() {
        createAnaDirect();
        createPedroDirect();
        UserController m = manager();
        assertThrows(UserException.class, () -> m.update(ANA, "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(UserException.class, () -> m.update(ANA, "Clave1!", UserField.EMAIL, PEDRO));
        assertThrows(UserException.class, () -> m.update(ANA, "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserException.class, () -> m.update(ANA, "Clave1!", UserField.CARD, "123"));
        assertThrows(UserException.class, () -> m.update(ANA, "Clave1!", UserField.NAME, ""));
        restart();
        User ana = manager().findByDni("12345678Z");
        assertEquals(ANA, ana.getEmail());
        assertDoesNotThrow(() -> manager().authenticate(ANA, "Clave1!"));
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Ana", ana.getName());
    }

    @Test
    public void updateKeepingSameEmailIsAllowed() {
        createAnaDirect();
        manager().update(ANA, "Clave1!", UserField.EMAIL, ANA);
        assertNotNull(manager().findByEmail(ANA));
    }

    @Test
    public void updateRequiresValidCredentials() {
        createAnaDirect();
        UserController m = manager();
        assertThrows(UserException.class, () -> m.update(ANA, "Mala1!", UserField.NAME, "X"));
        assertThrows(UserException.class, () -> m.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals("Ana", m.findByEmail(ANA).getName());
    }
}
