package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line, with fields separated by {@code ;}:
 * {@code Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta}.
 * This is the only class that reads or writes the user file.
 */
public class UserFileStorage {

    public static final String DEFAULT_FILE = "users.txt";
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserFileStorage(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /** Loads every stored user. A missing file means there are no users yet. */
    public List<User> load() {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read user file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                System.err.println("Skipping malformed user record: " + line);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /** Rewrites the file so that it contains exactly the given users. */
    public void save(List<User> users) {
        List<String> lines = new ArrayList<>(users.size());
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCard()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write user file " + file, e);
        }
    }
}
