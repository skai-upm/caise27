# caise27

Amusement park management application (Java 21, Maven, standard library only).

## Build

```text
mvn package
```

This produces the executable JAR `target/nombre-aplicacion.jar`.

## Run

Interactive mode (commands read from standard input):

```text
java -jar target/nombre-aplicacion.jar
```

File mode (one command per line; blank lines are ignored; processing stops at `EXIT`):

```text
java -jar target/nombre-aplicacion.jar input.txt
```

Users are stored in `users.txt` in the working directory and loaded at start-up.

## Commands

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
HELP
EXIT
```
