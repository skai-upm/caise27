# caise27

## Amusement Park

Java 21 command-line application for managing an amusement park. Delivery 1 covers user management. Delivery 2 adds groups, individual and group tickets, attractions, and a temporary execution mode. Delivery 3 adds typed attractions, SpeedPasses and Special Passes, invoices, extended help, and BCrypt-protected passwords.

### Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`. The only runtime library is jBCrypt (`org.mindrot:jbcrypt:0.4`), which protects passwords. It is bundled into the JAR, so `java -jar` needs no extra classpath.

### Run

Interactive mode (commands are read from standard input):

```text
java -jar target/amusement-park.jar
```

File mode (commands are read from a text file, one per line, blank lines ignored):

```text
java -jar target/amusement-park.jar input.txt
```

Both modes process commands identically. Processing stops at `EXIT` or at the end of the input.

Temporary mode (interactive or with a command file):

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input_fase2.txt
```

With `--fly` (only as the first argument), the run starts with no data and stores everything in a new `tmp/<yyyyMMdd_HHmmss>/` directory. That directory is deleted when the run ends, and the normal data files are never touched.

### Commands

Elements are separated by `;`. Keywords and field names are case-insensitive.

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>
SYSTEM DATE;<yyyy-MM-dd>
SYSTEM PRICE;<price>
GROUP CREATE;<identifier>;<name>
GROUP ADD;<identifier>;<email>
GROUP REMOVE;<identifier>;<email>
GROUP DELETE;<identifier>
TICKET CREATE;<identifier>;<email>;<date>
TICKET DELETE;<identifier>
GROUPTICKET CREATE;<identifier>;<group>;<date>
GROUPTICKET DELETE;<identifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
ATTRACTION CREATE;<identifier>;<name>;<capacity>
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attraction>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

`HELP EXTENDED` shows each command with a description of its purpose.

`<field>` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. The DNI cannot be modified.

Validation rules:

- DNI: 8 digits plus the correct Spanish check letter, e.g. `12345678Z`.
- Email: `text@text.xxx` with an extension of exactly three characters, e.g. `ana@correo.com`.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol, e.g. `Clave1!`.
- Card: exactly 16 digits.
- DNI and email must be unique. Emails are compared case-insensitively.

Successful operations print a confirmation such as `USER CREATED`, `GROUP CREATED`, or `TICKET CREATED. Price: 40.00 EUR`. Rejected commands print a line starting with `ERROR:` and the application keeps running.

Park rules:

- The system date starts as today's local date, and the base ticket price starts at 40.00 EUR in every run. Both can be changed with `SYSTEM DATE` and `SYSTEM PRICE`. A price change never affects existing tickets.
- Ticket identifiers are unique across all ticket types. A group ticket `TG01` contains one individual ticket per member, with identifier `TG01:<dni>`.
- Group discounts, based on the number of members: fewer than 3, 0%; 3 to 5, 10%; 6 to 10, 20%; more than 10, 50%. The discount is applied to each member's ticket, and the total is their sum.
- A ticket is active when it is dated today or later. Only tickets dated after today can be deleted; older ones are kept as historical records.
- While a group has an active group ticket, it cannot be changed (`GROUP ADD`, `REMOVE`, `DELETE`).
- A user with an active ticket, or in a locked group, cannot be deleted. Deleting a user removes them from their groups and from any attraction.
- `ATTRACTION ENTER` admits every holder of a ticket valid today, all together or not at all, within capacity. Users leave one by one with `ATTRACTION EXIT`.

Attraction types (`<type>`, default `GENERAL`):

| Type | Accepts | Passes |
| --- | --- | --- |
| `GENERAL` | individual and group tickets | optional `SPEED`: one SpeedPass per entering individual ticket |
| `INDIVIDUAL` | individual tickets | optional `SPEED`: one SpeedPass |
| `GROUP` | group tickets | optional `SPEED`: one SpeedPass for every ticket in the group ticket |
| `SPECIAL` | individual tickets | always uses the ticket's unconsumed Special Pass; `SPEED` is not allowed |

An individual ticket inside a group ticket (`TG01:<dni>`) counts as an individual ticket. If an entry fails for any reason, nobody enters and no pass is consumed.

Passes:

- A SpeedPass costs 20% of its ticket's stored price, so group discounts carry over. They are sold in 1 or 10 (`<purchase>:1` … `<purchase>:10`, with no package discount).
- A Special Pass costs the same as its ticket. A ticket can only ever have one.
- Passes can be bought for individual tickets dated today or later. They can be refunded (`... DELETE`) only while unconsumed and while the ticket is dated after today. Consumed passes are kept as purchase records.
- Deleting a ticket or group ticket also deletes all passes of its individual tickets.

Invoices list each item with its price and the total, and omit empty sections:

- A user invoice shows the user's direct individual tickets and their personal SpeedPasses and Special Passes, including passes on tickets inside group tickets.
- A group invoice shows the group's group tickets with their composition. The individual tickets inside are not charged again, and members' passes are not included.

Passwords are stored only as BCrypt hashes. User files from earlier versions with plain-text passwords are not migrated: those lines are skipped with a warning.

### Data files

Data is stored in the working directory (or the `--fly` temporary directory) as `;`-separated text files:

| File | Content |
| --- | --- |
| `users.txt` | `firstName;surnames;dni;email;passwordHash;phone;card` (BCrypt hash, never the password) |
| `groups.txt` | `id;name;memberDni;...` |
| `attractions.txt` | `id;name;capacity;TYPE` (occupants are not stored; attractions start empty; lines without a type are `GENERAL`) |
| `passes.txt` | `SPEED` or `SPECIAL`, then `;id;ticketId;price;consumed` |
| `tickets.txt` | `INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames`, or `GROUP;id;date;groupId;count` followed by `count` `MEMBER;...` lines |

Files are loaded at startup. After every change, the files it affects are rewritten together: either all of them are written or none. Tickets keep their holder's identity, so historical tickets of deleted users or groups survive restarts. Lines that are malformed or invalid are skipped with a warning on standard error.

### Design

The code is organized in layers under `es.upm.amusementpark`:

- `presentation`: command parsing, help generation, invoice text.
- `business`: model, Singleton controllers, invoice builders, exceptions, validation, password hashing.
- `persistence`: repository interfaces and their text-file implementations.

`App` (the entry point) and `ApplicationContext` (the composition root) create the repositories for the data directory and inject them, together with the collaborations between controllers, into the Singleton controllers. Normal and `--fly` runs differ only in that directory.

The class model is documented in PlantUML in [uml/model.puml](uml/model.puml). The tests `UmlModelTest` and `ArchitectureTest` check that the diagram and the layering match the code.
