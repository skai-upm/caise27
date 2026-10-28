# Amusement Park

Command-line application for managing the users of an amusement park (Delivery 1).

## Requirements

- JDK 21 (or newer, compiling for release 21)
- Maven 3

## Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`.

## Run

Interactive mode (commands are read from standard input):

```text
java -jar target/amusement-park.jar
```

File mode (commands are read from a text file, one command per line, in order; blank lines are ignored):

```text
java -jar target/amusement-park.jar input.txt
```

Users are stored in `users.txt` in the working directory (one user per line, attributes separated by `;`) and are loaded again on the next run.

## Commands

Elements of a command are separated by `;`.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>     field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
HELP
EXIT
```

Invalid commands or data are reported with a line starting with `ERROR:`; the application keeps running. After `EXIT` no further command is processed.
