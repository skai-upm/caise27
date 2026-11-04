package com.parque.persistence;

import com.parque.business.model.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores users in a text file, one user per line with fields separated by ';':
 * {@code Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta}.
 */
public class UserRepository {
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 7;

    private final Path file;

    public UserRepository(Path file) {
        this.file = file;
    }

    /** Loads every well-formed user line; a missing file means no users. */
    public List<User> load() throws IOException {
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) {
            return users;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != FIELDS) {
                System.err.println("Skipping malformed user line " + lineNumber + " in " + file);
                continue;
            }
            users.add(new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
        }
        return users;
    }

    /** Rewrites the file with exactly the given users. */
    public void save(List<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join(SEPARATOR, u.getName(), u.getSurname(), u.getDni(), u.getEmail(),
                    u.getPassword(), u.getPhone(), u.getCard()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
