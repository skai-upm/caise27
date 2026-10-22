# Amusement Park

Java 21 / Maven command-line application for managing an amusement park.

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

Command file mode (one command per line, blank lines ignored):

```text
java -jar target/amusement-park.jar input.txt
```

Users are stored in `users.txt` in the working directory and loaded on startup.

## Commands

Elements are separated by `;`.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>     field: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD
HELP
EXIT
```
