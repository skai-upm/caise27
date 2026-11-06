# caise27

Amusement Park application (Java, Maven).

## Prerequisites

- JDK 21 or newer (the build compiles with `--release 21`)
- Apache Maven 3.9+

## Build and test

```bash
mvn clean test
```

## Running

Without arguments the application reads commands from standard input, one per line.
With a file path as the first argument it processes the commands in that file.

General commands:

- `HELP` lists the available commands and their syntax.
- `EXIT` ends the application. No command after `EXIT` is executed, including later lines of a command file.
