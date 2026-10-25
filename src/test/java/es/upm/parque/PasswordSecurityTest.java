package es.upm.parque;

import es.upm.parque.business.security.BCryptPasswordHasher;
import es.upm.parque.persistence.TextUserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PasswordSecurityTest extends ParkTestSupport {

    private String usersFile() throws Exception {
        return Files.readString(dataDir.resolve(TextUserRepository.FILE_NAME));
    }

    @Test
    public void storesBcryptHashNotPlaintext() throws Exception {
        createAna();
        String line = Files.readAllLines(dataDir.resolve(TextUserRepository.FILE_NAME)).get(0);
        assertFalse(line.contains("Clave1!"));
        String[] fields = line.split(";");
        assertTrue(line.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;"), line);
        assertTrue(line.endsWith(";612345678;1234567890123456"), line);
        assertTrue(FAST_HASHER.isHash(fields[4]), fields[4]);
        assertTrue(FAST_HASHER.matches("Clave1!", fields[4]));
    }

    @Test
    public void formatRulesStillApply() {
        fails("USER CREATE;Ana;Garcia Lopez;12345678Z;" + ANA + ";clave1!;612345678;1234567890123456");
        createAna();
        fails("USER UPDATE;" + ANA + ";Clave1!;PASSWORD;CLAVE1!");
    }

    @Test
    public void verifiesPasswordsForUpdateAndDelete() {
        createAna();
        fails("USER UPDATE;" + ANA + ";Mala1!;NAME;X");
        fails("USER DELETE;" + ANA + ";Mala1!");
        ok("USER DELETE;" + ANA + ";Clave1!");
    }

    @Test
    public void passwordChangePersistsNewHash() throws Exception {
        createAna();
        String before = usersFile();
        ok("USER UPDATE;" + ANA + ";Clave1!;PASSWORD;Nueva2#");
        String after = usersFile();
        assertFalse(after.equals(before));
        assertFalse(after.contains("Nueva2#"));
        restart();
        fails("USER UPDATE;" + ANA + ";Clave1!;NAME;X");
        ok("USER UPDATE;" + ANA + ";Nueva2#;NAME;Ana Maria");
    }

    @Test
    public void legacyPlaintextIsMigratedOnLoad() throws Exception {
        Files.write(dataDir.resolve(TextUserRepository.FILE_NAME),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        restart();
        assertFalse(usersFile().contains("Clave1!"));
        ok("USER UPDATE;" + ANA + ";Clave1!;PHONE;699999999");
    }

    @Test
    public void hasherRecognisesItsHashes() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        String hash = hasher.hash("Clave1!");
        assertEquals(60, hash.length());
        assertTrue(hasher.isHash(hash));
        assertFalse(hasher.isHash("Clave1!"));
        assertFalse(hasher.matches("Clave1!", "not-a-hash"));
        assertFalse(hasher.matches("Otra1!", hash));
    }
}
