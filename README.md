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

Temporary mode runs with an isolated, throw-away data directory:

```bash
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input.txt
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

Group ticket commands:

- `GROUPTICKET CREATE;<identifier>;<group>;<date>` buys a joint ticket for all current members of a non-empty group. It contains one individual ticket per member, identified `<identifier>:<dni>`. The total price is displayed.
- `GROUPTICKET DELETE;<identifier>` deletes a group ticket dated strictly after the current date, together with its individual tickets.

Group discount on the base price of each member's ticket: fewer than 3 members 0%, 3-5 members 10%, 6-10 members 20%, more than 10 members 50%. Individual tickets that belong to a group ticket cannot be deleted with `TICKET DELETE`.

Attraction commands:

- `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>` creates an attraction with a positive maximum capacity.
- `ATTRACTION LIST;<AttractionIdentifier>` shows the emails of the users currently inside it (read-only).
- `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>` admits the ticket's user(s) when the ticket is dated exactly the current system date and the attraction has room for all of them. The date is checked only at entry.
- With a group ticket, all its users enter together or nobody enters.
- `ATTRACTION EXIT;<AttractionIdentifier>;<Email>` lets a user leave and frees their place.
- `ATTRACTION STATUS;<AttractionIdentifier>` shows the identifier, name, capacity, number of users inside, available places and the users inside.

## Persistence

Groups, tickets (including group tickets and their composition) and attractions are stored as text files in `data/` and reloaded at startup. Attractions always start empty. Historical tickets are kept even after their user or group is deleted. With `--fly`, all files go to a new `tmp/<yyyyMMdd_HHmmss>/` directory, which is deleted when the execution ends.

## Design model

The PlantUML class model is in [`uml/model.puml`](uml/model.puml). `UmlModelConsistencyTest` fails if a type of the model, controller or persistence packages is missing from it.

## Delivery 2 example session

```text
SYSTEM DATE;2027-04-15
GROUP CREATE;G01;Familia Garcia
ATTRACTION CREATE;A01;Dragon Rojo;20
GROUPTICKET CREATE;TG01;G01;2027-04-15
ATTRACTION ENTER;A01;TG01
ATTRACTION STATUS;A01
EXIT
```

Save it as `input_fase2.txt` and run `java -jar target/amusement-park.jar --fly input_fase2.txt` to try it without changing `data/`. Group members and ticket owners must be registered users.
