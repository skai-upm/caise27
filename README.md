# caise27

## Amusement Park

Java 21 / Maven application for managing an amusement park. Delivery 1 provides user management through a text command interface with file-based persistence.

### Build

```text
mvn package
```

This runs the tests and produces `target/amusement-park.jar`. Only the Java standard library is used at runtime.

### Run

Interactive mode (commands are read from standard input until `EXIT`):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line; blank lines are ignored; processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

A missing or unreadable command file prints an error and exits with status 1.

### Commands

Elements are separated by `;`. Command keywords are case-insensitive.

| Command | Description |
|---|---|
| `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>` | Register a user |
| `USER DELETE;<email>;<password>` | Delete a user after checking the password |
| `USER UPDATE;<email>;<password>;<field>;<value>` | Change one field: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (the DNI cannot be changed) |
| `HELP` | Show the available commands |
| `EXIT` | Terminate the application |

Validation rules:

- **DNI**: 8 digits plus the matching Spanish control letter (e.g. `12345678Z`).
- **Email**: `text@text.xxx`, with an extension of exactly three letters (e.g. `ana@correo.com`).
- **Password**: at least one uppercase letter, one lowercase letter, one digit and one symbol (e.g. `Clave1!`).
- **Card**: exactly 16 digits.
- DNI and email must be unique among users.

Successful operations print `USER CREATED`, `USER UPDATED` or `USER DELETED`; rejected ones print a line starting with `ERROR:`.

Example `input.txt`:

```text
USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456
USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654
USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999
USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com
USER DELETE;pedro@correo.com;Otra2#
HELP
EXIT
```

### Data file

Users are stored in `users.txt` in the working directory, one per line:

```text
Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456
```

The file is loaded at start-up and rewritten after each successful change. Set the `park.users.file` system property to use another location, e.g. `java -Dpark.users.file=/tmp/users.txt -jar target/amusement-park.jar`.
