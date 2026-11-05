# Amusement Park

Command-line application for managing the users of an amusement park (Delivery 1).

## Requirements

- JDK 21 or newer (the build targets Java 21 with `maven.compiler.release`).
- Maven 3.9 or newer.

The application only uses the Java standard library.

## Build

```text
mvn package
```

This compiles the sources, runs the tests and produces `target/amusement-park.jar`.

## Run

Interactive mode reads commands from standard input, one per line:

```text
java -jar target/amusement-park.jar
```

File mode processes the commands of a text file in order. Blank lines are ignored.

```text
java -jar target/amusement-park.jar input.txt
```

Commands are processed identically in both modes. `EXIT` stops processing, and no later line is executed. A nonexistent or unreadable command file is reported with an `ERROR` line.

## Commands

Elements are separated by `;`.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>    field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
HELP
EXIT
```

Successful commands answer with a line starting with `OK`. Rejected or unrecognised commands answer with a line starting with `ERROR` and do not change any data.

Validation rules:

- DNI: eight digits plus the matching check letter.
- Email: `text@text.xxx` with a three-character extension.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
- Card: exactly 16 digits.
- DNI and email must be unique. The DNI cannot be modified.

## Persistence

Users are stored in `users.txt` in the working directory, one user per line:

```text
FirstName;Surnames;DNI;Email;Password;Phone;Card
```

The file is loaded at start-up and rewritten after every successful change. Lines that are malformed, invalid or duplicated are ignored with a `WARNING` and dropped at the next save. If `users.txt` exists but cannot be read, the application prints an `ERROR` and does not process any command.
