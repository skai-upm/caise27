# Amusement Park

Command-line application for managing an amusement park: users, groups, individual and group tickets, and attractions (Deliveries 1 and 2).

## Requirements

- JDK 21 or newer (the build targets Java 21 with `maven.compiler.release`).
- Maven 3.9 or newer.

The application only uses the Java standard library.

## Build

```text
mvn package
```

This compiles the sources, runs the tests and produces `target/amusement-park.jar`.

## Run

Interactive mode reads commands from standard input, one per line:

```text
java -jar target/amusement-park.jar
```

File mode processes the commands of a text file in order. Blank lines are ignored.

```text
java -jar target/amusement-park.jar input.txt
```

Commands are processed identically in both modes. `EXIT` stops processing, and no later line is executed. A nonexistent or unreadable command file is reported with an `ERROR` line.

## Commands

Elements are separated by `;`. `HELP` prints this list.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>     Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
SYSTEM DATE;<yyyy-MM-dd>
SYSTEM PRICE;<Price>
GROUP CREATE;<Identifier>;<Name>
GROUP ADD;<Identifier>;<Email>
GROUP REMOVE;<Identifier>;<Email>
GROUP DELETE;<Identifier>
TICKET CREATE;<identifier>;<email>;<date>
TICKET DELETE;<identifier>
GROUPTICKET CREATE;<identifier>;<group>;<date>
GROUPTICKET DELETE;<identifier>
ATTRACTION CREATE;<identifier>;<name>;<capacity>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
EXIT
```

Park rules in short:

- The system date starts as today, and the base price starts at 40.00 euros on every run. `SYSTEM DATE` and `SYSTEM PRICE` change them.
- A ticket is valid only on its date. Tickets for today or earlier cannot be deleted; they are kept as history.
- A group ticket contains one individual ticket per member, identified `<groupTicket>:<dni>`. Each member's ticket gets a member-count discount: fewer than 3 members, 0%; 3–5, 10%; 6–10, 20%; more than 10, 50%.
- An active group ticket (dated today or later) locks the group against `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE`.
- A user with an active ticket, or in a locked group, cannot be deleted.
- Group entry to an attraction is all-or-nothing within its capacity.
- Invoices are generated on demand and never stored. A user invoice lists the user's own individual tickets; tickets that are part of a group ticket are excluded because the group ticket already includes them. A group invoice lists the group's tickets once, at their total price, and shows each ticket's composition as detail. Empty sections are left out.

Successful commands answer with a line starting with `OK`. Rejected or unrecognised commands answer with a line starting with `ERROR` and do not change any data.

Validation rules:

- DNI: eight digits plus the matching check letter.
- Email: `text@text.xxx` with a three-character extension.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
- Card: exactly 16 digits.
- DNI and email must be unique. The DNI cannot be modified.

## Persistence

Data is stored in text files in the working directory:

| File | Content |
| --- | --- |
| `users.txt` | `FirstName;Surnames;DNI;Email;Password;Phone;Card` |
| `groups.txt` | `id;name;dni1,dni2,...` |
| `tickets.txt` | `I;id;date;price;dni;email;firstName;surnames` and `G;id;date;group;component1,component2,...` |
| `attractions.txt` | `id;name;capacity` (users inside are not stored; attractions start empty) |

Files are loaded at start-up and rewritten after every successful command. Historical tickets keep their holder data even after the user or group is deleted. Malformed, invalid or duplicated lines are ignored, with a `WARNING`. If a data file exists but cannot be read, the application prints an `ERROR` and processes no command.

### Temporary mode

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input_fase2.txt
```

With `--fly` as the first argument, all data files are written in a new `tmp/<yyyyMMdd_HHmmss>/` directory, which is deleted when the run ends (`EXIT` or end of input). Normal data files are never touched in this mode.
