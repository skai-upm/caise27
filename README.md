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

System commands (park-wide settings for the current execution):

- `SYSTEM DATE;<yyyy-MM-dd>` sets the current system date. It starts as the local date when the application starts.
- `SYSTEM PRICE;<Price>` sets the current base ticket price, a positive amount with at most two decimals. It starts at 40.00 euros on every execution.

Group commands:

- `GROUP CREATE;<Identifier>;<Name>` creates an empty group with a unique identifier.
- `GROUP ADD;<Identifier>;<Email>` adds a registered user to a group (at most once).
- `GROUP REMOVE;<Identifier>;<Email>` removes a member from a group.
- `GROUP DELETE;<Identifier>` deletes a group.

A group with a group ticket dated today or later cannot be modified or deleted.

Ticket commands:

- `TICKET CREATE;<identifier>;<email>;<date>` buys an individual ticket for a registered user, dated today or later, at the current base price. The price is displayed.
- `TICKET DELETE;<identifier>` deletes an individual ticket dated strictly after the current date. Tickets for today or earlier are kept as historical records.

A user with a ticket dated today or later, or who belongs to a group with an active group ticket, cannot be deleted.
