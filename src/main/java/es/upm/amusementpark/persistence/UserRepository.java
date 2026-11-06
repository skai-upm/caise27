package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import es.upm.amusementpark.business.User;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists users, one per line: {@code FirstName;Surnames;DNI;Email;Password;Phone;Card}. The password field holds
 * the stored protected (BCrypt) value.
 */
public class UserRepository {

    private final TextFile file;

    public UserRepository(Path path) {
        this.file = new TextFile(path);
    }

    public void load(ParkController park, List<String> warnings) throws IOException {
        List<String> lines = file.readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFile.fields(line);
            try {
                if (f.length != 7) {
                    throw new IllegalArgumentException("expected 7 fields");
                }
                park.restoreUser(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
            } catch (IllegalArgumentException | NullPointerException | ParkException e) {
                warnings.add(file.getPath().getFileName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : park.getUsers()) {
            lines.add(TextFile.join(u.getFirstName(), u.getSurnames(), u.getDni(), u.getEmail(), u.getPassword(),
                    u.getPhone(), u.getCard()));
        }
        file.writeLines(lines);
    }
}
