# caise27 — Amusement Park

Java 21 / Maven console application that manages the users of an amusement park.

## Build

```bash
mvn package
```

Produces the executable JAR `target/amusement-park.jar` (standard library only).

## Run

Interactive mode (commands typed on standard input):

```bash
java -jar target/amusement-park.jar
```

File mode (one command per line, processed in order, blank lines ignored):

```bash
java -jar target/amusement-park.jar input.txt
```

Users are stored in `users.txt` in the working directory, one `;`-separated line per user.

## Commands

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>    (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
HELP
EXIT
```

Incorrect commands print an `Error: ...` message and processing continues; `EXIT` stops processing.
