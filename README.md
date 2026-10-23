# caise27

## Amusement Park

Java 21 / Maven application for managing an amusement park: users, groups, individual and group tickets, SpeedPasses and Special Passes, typed attractions and invoices, driven by a text command interface with file-based persistence.

### Build

```text
mvn package
```

This runs the tests and produces the self-contained `target/amusement-park.jar`. At runtime only the Java standard library and jBCrypt (bundled into the JAR by the shade plugin) are used.

### Run

Interactive mode (commands are read from standard input until `EXIT`):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line; blank lines are ignored; processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

Temporary mode (`--fly` as first argument, optionally followed by a command file): all data is kept in a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the run ends, so permanent data is never touched:

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input_fase2.txt
```

A missing or unreadable command file prints an error and exits with status 1.

### Commands

Elements are separated by `;`. Command keywords are case-insensitive.

| Command | Description |
|---|---|
| `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>` | Register a user |
| `USER DELETE;<email>;<password>` | Delete a user after checking the password |
| `USER UPDATE;<email>;<password>;<field>;<value>` | Change one field: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (the DNI cannot be changed) |
| `SYSTEM DATE;<yyyy-MM-dd>` | Set the current system date (initially today) |
| `SYSTEM PRICE;<price>` | Set the base ticket price (initially 40.00 EUR each run) |
| `GROUP CREATE;<identifier>;<name>` | Create an empty group |
| `GROUP ADD;<identifier>;<email>` | Add a user to a group |
| `GROUP REMOVE;<identifier>;<email>` | Remove a user from a group |
| `GROUP DELETE;<identifier>` | Delete a group |
| `TICKET CREATE;<identifier>;<email>;<date>` | Buy an individual ticket for today or a later date |
| `TICKET DELETE;<identifier>` | Delete an individual ticket dated after today |
| `GROUPTICKET CREATE;<identifier>;<group>;<date>` | Buy a ticket for every member of a group (ids `<identifier>:<dni>`) |
| `GROUPTICKET DELETE;<identifier>` | Delete a group ticket dated after today, with its member tickets |
| `ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]` | Create an attraction of type `GENERAL` (default), `INDIVIDUAL`, `GROUP` or `SPECIAL` |
| `ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]` | Enter with a ticket valid today (a group ticket admits all members or nobody); `SPEED` uses one SpeedPass per ticket |
| `ATTRACTION EXIT;<attraction>;<email>` | Leave an attraction |
| `ATTRACTION LIST;<attraction>` | Show the emails of the users inside |
| `ATTRACTION STATUS;<attraction>` | Show identifier, name, type, capacity, occupancy, free places and users inside |
| `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` | Buy 1 or 10 SpeedPasses (`<purchaseIdentifier>:1..n`), 20% of the ticket price each |
| `SPEEDPASS DELETE;<speedPassIdentifier>` | Refund an unconsumed SpeedPass of a ticket dated after today |
| `SPECIALPASS CREATE;<identifier>;<individualTicket>` | Buy the ticket's only Special Pass (same price as the ticket) |
| `SPECIALPASS DELETE;<specialPassIdentifier>` | Refund an unconsumed Special Pass of a ticket dated after today |
| `INVOICE CREATE;USER;<email>` | Invoice of a user's direct tickets and passes |
| `INVOICE CREATE;GROUP;<groupIdentifier>` | Invoice of a group's group tickets |
| `HELP` | Show the available commands |
| `HELP EXTENDED` | Show the commands with a description of each |
| `EXIT` | Terminate the application |

Validation rules:

- **DNI**: 8 digits plus the matching Spanish control letter (e.g. `12345678Z`).
- **Email**: `text@text.xxx`, with an extension of exactly three letters (e.g. `ana@correo.com`).
- **Password**: at least one uppercase letter, one lowercase letter, one digit and one symbol (e.g. `Clave1!`).
- **Card**: exactly 16 digits.
- DNI and email must be unique among users.

Park rules:

- A ticket is **active** when dated today (system date) or later, otherwise **historical**. Only tickets dated strictly after today can be deleted.
- Group ticket discount per member ticket: fewer than 3 members 0%, 3–5 10%, 6–10 20%, more than 10 50%. The total is the sum of the member tickets (e.g. 6 members at 40.00 → 192.00 EUR).
- A group with an active group ticket is locked: no `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE`.
- `USER DELETE` is rejected while the user has an active ticket or belongs to a locked group; otherwise the user is also removed from their groups. Historical tickets are kept.
- Ticket identifiers (individual, group and member tickets) share one identifier space.
- A user can be inside only one attraction at a time.
- Attraction types: `GENERAL` accepts individual and group tickets, `INDIVIDUAL` and `SPECIAL` only individual tickets (member tickets of a group ticket included), `GROUP` only group tickets. `SPECIAL` entry consumes the ticket's Special Pass automatically and does not accept `SPEED`.
- Passes belong to an individual ticket and keep the price computed at purchase; consumed passes stay recorded. Deleting a ticket (or group ticket) deletes its passes.
- Invoices never count member tickets of a group ticket as personal purchases; members' passes appear on their user invoices, not on the group invoice.
- Passwords are stored as BCrypt hashes. `users.txt` files from earlier versions (plain-text passwords) are not migrated and their users cannot authenticate.

Successful operations print a confirmation (e.g. `USER CREATED`, `GROUP TICKET CREATED: ...`); rejected ones print a line starting with `ERROR:`.

Example `input.txt`:

```text
USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456
USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654
USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999
USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com
USER DELETE;pedro@correo.com;Otra2#
HELP
EXIT
```

### Data files

Data is stored in the working directory (or the `--fly` temporary directory), loaded at start-up and rewritten after each successful change:

| File | Line format |
|---|---|
| `users.txt` | `firstName;surnames;dni;email;bcryptHash;phone;card` |
| `groups.txt` | `id;name;dni1,dni2,...` |
| `tickets.txt` | `INDIVIDUAL;id;date;basePrice;discount;A/F;dni;email;firstName;surnames`, `GROUP;id;date;groupId`, `MEMBER;groupTicketId;id;date;basePrice;discount;A/F;dni;email;firstName;surnames` |
| `passes.txt` | `SPEED` or `SPECIAL;id;ticketId;price;consumed` |
| `attractions.txt` | `id;name;capacity;type` (lines without type load as `GENERAL`) |

In `tickets.txt`, `A`/`F` marks whether the ticket's user was active or already deleted, so historical tickets survive user deletion. The users inside attractions are not stored: attractions start empty. Set the `park.data.dir` system property to use another directory, e.g. `java -Dpark.data.dir=/tmp/park -jar target/amusement-park.jar`.

### Design

Code is organised in layers under `es.upm.etsisi.park`:

- `presentation`: `CommandProcessor`, a `CommandRegistry` filled by command groups, and help generation with injected `HelpRenderer`s.
- `business`: singleton controllers (`SystemController`, `UserController`, `GroupController`, `TicketController`, `PassController`, `AttractionController`, `InvoiceController`), the domain model (tickets with `Access`/`IndividualAccess`/`GroupAccess` capabilities, generic `Attraction<T extends Access>` types, passes, invoices built with `InvoiceBuilder`), the `ParkException` hierarchy and the `PasswordHasher`.
- `persistence`: repository interfaces and their text-file implementations.

`ApplicationContext` wires everything at start-up: the repositories (for the normal or `--fly` directory) and the controllers' collaborators are injected by hand; no DI framework is used.

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
