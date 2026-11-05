package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.business.model.User;
import es.upm.parque.persistence.UserRepository;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        processor = TestSupport.processor(repo);
    }

    private static String create(String dni, String email, String password, String card) {
        return "USER CREATE;Ana;Garcia Lopez;" + dni + ";" + email + ";" + password + ";612345678;" + card;
    }

    @Test
    void helpListsAllCommandSyntaxes() {
        String help = processor.process("HELP");
        for (String syntax : new String[] {
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>",
                "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
                "HELP", "EXIT"}) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertTrue(processor.process("HELP;x").startsWith("ERROR"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void helpListsTheAssignmentSyntaxInOrder() {
        String help = processor.process("HELP");
        int from = 0;
        for (String syntax : CommandProcessor.HELP_SYNTAX) {
            int at = help.indexOf(syntax, from);
            assertTrue(at >= from, "missing or out of order: " + syntax);
            from = at + syntax.length();
        }
        assertEquals(22, CommandProcessor.HELP_SYNTAX.size());
    }

    @Test
    void exitSetsTheExitFlag() {
        assertTrue(processor.process("EXIT;x").startsWith("ERROR"));
        assertFalse(processor.isExitRequested());
        assertTrue(processor.process("EXIT").startsWith("OK"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void blankLinesProduceNoResponse() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
        assertNull(processor.process(null));
    }

    @Test
    void unknownCommandIsAnErrorAndKeepsState() {
        assertTrue(processor.process("FOO;bar").startsWith("ERROR"));
        assertTrue(processor.process("USER PROMOTE;ana@correo.com").startsWith("ERROR"));
        assertEquals(0, repo.size());
    }

    private static final String LUIS =
            "USER CREATE;Luis;Perez;87654321X;luis@correo.com;Clave1!;600000000;1234567890123457";

    private String update(String field, String value) {
        return processor.process("USER UPDATE;ana@correo.com;Clave1!;" + field + ";" + value);
    }

    @Test
    void eachModifiableFieldIsUpdated() {
        processor.process(ANA);
        assertTrue(update("NAME", "Ana Maria").startsWith("OK"));
        assertTrue(update("SURNAME", "Garcia Ruiz").startsWith("OK"));
        assertTrue(update("PHONE", "+34 699 000 111").startsWith("OK"));
        assertTrue(update("CARD", "9999888877776666").startsWith("OK"));
        User u = repo.findByDni("12345678Z");
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia Ruiz", u.getSurnames());
        assertEquals("+34 699 000 111", u.getTelephone());
        assertEquals("9999888877776666", u.getCardNumber());

        assertTrue(update("PASSWORD", "Nueva2#").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;X").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Nueva2#;EMAIL;ana.nueva@correo.com").startsWith("OK"));
        assertEquals("12345678Z", repo.findByEmail("ana.nueva@correo.com").getDni());
    }

    @Test
    void updateRequiresAuthentication() {
        processor.process(ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Mala1!;NAME;Ana Maria").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;nadie@correo.com;Clave1!;NAME;X").startsWith("ERROR"));
        assertEquals("Ana", repo.findByDni("12345678Z").getFirstName());
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(ANA);
        assertTrue(update("DNI", "87654321X").startsWith("ERROR"));
        assertTrue(update("AGE", "30").startsWith("ERROR"));
        assertTrue(update("name", "x").startsWith("ERROR"));
        assertEquals("12345678Z", repo.findByEmail("ana@correo.com").getDni());
        assertEquals(1, repo.size());
    }

    @Test
    void invalidNewValuesAreRejected() {
        processor.process(ANA);
        processor.process(LUIS);
        assertTrue(update("EMAIL", "ana@correo.es").startsWith("ERROR"));
        assertTrue(update("EMAIL", "luis@correo.com").startsWith("ERROR"));
        assertTrue(update("PASSWORD", "Clave12").startsWith("ERROR"));
        assertTrue(update("CARD", "999988887777666").startsWith("ERROR"));
        User u = repo.findByDni("12345678Z");
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void ownEmailIsAcceptedAndWrongCountRejected() {
        processor.process(ANA);
        assertTrue(update("EMAIL", "ana@correo.com").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;a;b").startsWith("ERROR"));
    }

    @Test
    void deleteWithMatchingEmailAndPasswordRemovesUser() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertEquals(0, repo.size());
        assertTrue(processor.process(ANA).startsWith("OK"), "DNI and email reusable after deletion");
    }

    @Test
    void deleteWithWrongPasswordKeepsUser() {
        processor.process(ANA);
        String r = processor.process("USER DELETE;ana@correo.com;Otra1!");
        assertTrue(r.startsWith("ERROR") && r.contains("password"), r);
        assertEquals(1, repo.size());
        assertEquals("Clave1!", repo.findByDni("12345678Z").getPassword());
    }

    @Test
    void deleteUnknownEmailIsRejected() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;nadie@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals(1, repo.size());
    }

    @Test
    void deleteWithWrongElementCountIsRejected() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!;x").startsWith("ERROR"));
        assertEquals(1, repo.size());
    }

    @Test
    void validUserIsCreatedWithAllAttributes() {
        assertTrue(processor.process(ANA).startsWith("OK"));
        User u = repo.findByDni("12345678Z");
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getTelephone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void wrongNumberOfElementsIsRejected() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678").startsWith("ERROR"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR"));
        assertEquals(0, repo.size());
    }

    @Test
    void invalidFieldsAreRejectedNamingTheField() {
        String r;
        r = processor.process(create("12345678A", "ana@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("DNI"), r);
        r = processor.process(create("12345678Z", "ana@correo.es", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("email"), r);
        r = processor.process(create("12345678Z", "ana@correo.com", "Clave12", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("password"), r);
        r = processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "123456789012345"));
        assertTrue(r.startsWith("ERROR") && r.contains("card"), r);
        assertEquals(0, repo.size());
    }

    @Test
    void duplicateDniIsRejected() {
        processor.process(ANA);
        String r = processor.process(create("12345678Z", "otra@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("DNI"), r);
        assertEquals(1, repo.size());
        assertEquals("ana@correo.com", repo.findByDni("12345678Z").getEmail());
    }

    @Test
    void duplicateEmailIsRejected() {
        processor.process(ANA);
        String r = processor.process(create("87654321X", "ana@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("email"), r);
        assertEquals(1, repo.size());
    }
}
