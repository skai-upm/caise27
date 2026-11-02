package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.park.ParkData;
import es.upm.etsisi.amusementpark.business.park.ParkRepository;
import es.upm.etsisi.amusementpark.business.user.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.persistence.FileParkRepository;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/** Shared test wiring: the real composition root with fast BCrypt and a fixed date. */
public final class TestSupport {

    public static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    public static final BCryptPasswordHasher HASHER = new BCryptPasswordHasher(4);
    public static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    public static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    public static final String LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000001;1111222233334444";

    private TestSupport() {
    }

    public static CommandProcessor start(Path dir) throws StorageException {
        return ApplicationContext.start(fileRepository(dir), TODAY, HASHER);
    }

    public static FileParkRepository fileRepository(Path dir) {
        return new FileParkRepository(dir, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    /** Runs each line and returns the outputs. */
    public static List<String> run(CommandProcessor processor, String... lines) {
        return java.util.Arrays.stream(lines).map(l -> processor.process(l).output()).toList();
    }

    /** A valid user-creation command whose DNI number is {@code n}. */
    public static String user(int n) {
        String number = String.format("%08d", n);
        char letter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
        return "USER CREATE;User" + n + ";Surname;" + number + letter + ";u" + n + "@correo.com;Clave1!;600;1234567890123456";
    }

    /** In-memory repository that can be told to fail on save. */
    public static final class MemoryRepository implements ParkRepository {
        public ParkData stored = new ParkData(List.of(), List.of(), List.of(), List.of(), List.of());
        public boolean failOnSave;
        public int saves;

        @Override
        public ParkData load() {
            return stored;
        }

        @Override
        public void save(ParkData data) throws StorageException {
            if (failOnSave) {
                throw new StorageException("disk full");
            }
            saves++;
            stored = data;
        }
    }
}
