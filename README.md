# caise27

Amusement Park application (Java, Maven).

## Prerequisites

- JDK 21 or newer (the build compiles with `--release 21`)
- Apache Maven 3.9+

## Build and test

```bash
mvn clean test
```

## Packaging

```bash
mvn package
```

This produces the executable JAR `target/amusement-park.jar`. It uses only the Java standard library at runtime.

## Running

Interactive mode reads commands from standard input, one per line:

```bash
java -jar target/amusement-park.jar
```

File mode runs the commands of a text file in order:

```bash
java -jar target/amusement-park.jar input.txt
```

Blank lines are ignored. Unknown commands and commands that fail are reported with an `ERROR:` message, and processing continues. A command file that does not exist or cannot be read is reported, and no command runs.

General commands:

- `HELP` lists the available commands and their syntax.
- `EXIT` ends the application. No command after `EXIT` is executed, including later lines of a command file.
