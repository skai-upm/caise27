# caise27

## Amusement Park

Java 21 / Maven command-line application for managing amusement park users.

### Build

```text
mvn package
```

This runs the tests and produces the executable JAR `target/amusement-park.jar`.

### Run

Interactive mode (commands read from standard input until `EXIT` or end of input):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line, blank lines ignored, processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

### Commands

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>    (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
HELP
EXIT
```

Rejected commands print a line starting with `ERROR:` and the application keeps running.

### Data file

Users are stored in `users.txt` in the working directory, one user per line:

```text
FirstName;Surnames;DNI;Email;Password;Phone;Card
```

The location can be overridden with `-Dpark.users.file=<path>`, e.g.
`java -Dpark.users.file=data/users.txt -jar target/amusement-park.jar`.
