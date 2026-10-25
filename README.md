# caise27 — Amusement Park

Java 21 / Maven command-line application for managing an amusement park.
Delivery 1 provides user management and the execution infrastructure.

## Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`.

## Run

Interactive mode (commands read from standard input):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line, blank lines ignored, processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

Users are stored in `users.txt` in the working directory. Another location can be
chosen with `-Dpark.users.file=<path>`:

```text
java -Dpark.users.file=/tmp/users.txt -jar target/amusement-park.jar input.txt
```

## Commands

Elements are separated by `;`. Command keywords are case-insensitive.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>
HELP
EXIT
```

`<field>` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (the DNI cannot be changed).

Validation rules:

- DNI: eight digits plus the matching control letter (e.g. `12345678Z`).
- Email: `text@text.xxx` with an extension of exactly three characters.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
- Card: exactly 16 digits.

Successful operations print `USER CREATED`, `USER DELETED` or `USER UPDATED`;
rejected ones print a line starting with `ERROR`.

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
