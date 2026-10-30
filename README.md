# caise27

## Amusement Park

Command-line application (Java 21, Maven) that manages amusement park users.

### Build

```text
mvn package
```

This runs the tests and produces `target/amusement-park.jar`.

### Run

Interactive mode (commands are read from standard input until `EXIT`):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line, blank lines ignored, processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

### Commands

Elements are separated by `;`.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>   field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
HELP
EXIT
```

Rejected commands print a line starting with `ERROR:` and the application keeps running.

### Data

Users are stored in `users.txt` in the working directory, one user per line:

```text
FirstName;Surnames;DNI;Email;Password;Phone;Card
```
