# caise27

## Amusement Park

Java 21 command-line application for managing an amusement park. Delivery 1 covers user management.

### Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`. The application uses only the Java standard library.

### Run

Interactive mode (commands are read from standard input):

```text
java -jar target/amusement-park.jar
```

File mode (commands are read from a text file, one per line, blank lines ignored):

```text
java -jar target/amusement-park.jar input.txt
```

Both modes process commands identically. Processing stops at `EXIT` or at the end of the input.

### Commands

Elements are separated by `;`. Keywords and field names are case-insensitive.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>
HELP
EXIT
```

`<field>` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. The DNI cannot be modified.

Validation rules:

- DNI: 8 digits plus the correct Spanish check letter, e.g. `12345678Z`.
- Email: `text@text.xxx` with an extension of exactly three characters, e.g. `ana@correo.com`.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol, e.g. `Clave1!`.
- Card: exactly 16 digits.
- DNI and email must be unique. Emails are compared case-insensitively.

Successful operations print `USER CREATED`, `USER DELETED` or `USER UPDATED`. Rejected commands print a line starting with `ERROR:` and the application keeps running.

### Data file

Users are stored in `users.txt` in the working directory, one user per line:

```text
Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456
```

The file is loaded at startup and rewritten after every change. Lines that are malformed or invalid are skipped with a warning on standard error.
