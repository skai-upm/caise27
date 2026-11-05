package es.upm.amusementpark.presentation;

import es.upm.amusementpark.persistence.repository.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        processor = ApplicationInitializer.initialize(Repositories.inMemory(repo));
    }

    @Test
    void createsUser() {
        assertEquals("USER CREATED", processor.process(ANA));
        assertEquals(1, repo.count());
    }

    @Test
    void duplicateRejected() {
        processor.process(ANA);
        assertTrue(processor.process("USER CREATE;Luis;Martin;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444").startsWith("ERROR"));
        assertTrue(processor.process("USER CREATE;Luis;Martin;87654321X;ana@correo.com;Clave1!;600000000;1111222233334444").startsWith("ERROR"));
        assertEquals(1, repo.count());
    }

    @Test
    void invalidDataRejected() {
        assertTrue(processor.process(ANA.replace("12345678Z", "12345678A")).startsWith("ERROR"));
        assertEquals(0, repo.count());
    }

    @Test
    void wrongArgumentCountAndUnknownCommand() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia").startsWith("ERROR"));
        assertTrue(processor.process("FOO;bar").startsWith("ERROR"));
        assertEquals(0, repo.count());
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void blankLineIgnoredAndWhitespaceTrimmed() {
        assertEquals("", processor.process("   "));
        assertEquals("USER CREATED", processor.process(" USER CREATE ; Ana;Garcia;12345678Z ;ana@correo.com;Clave1!;6;1234567890123456 "));
    }
}
