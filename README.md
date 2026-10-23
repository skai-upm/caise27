# caise27

## Amusement Park

Java 21 / Maven command-line application for managing an amusement park: users, groups, individual and group tickets, SpeedPasses and Special Passes, typed attractions and invoices.

### Build

```text
mvn package
```

This runs the tests and produces the self-contained executable JAR `target/amusement-park.jar` (it bundles the jBCrypt dependency).

### Run

```text
java -jar target/amusement-park.jar                 # interactive
java -jar target/amusement-park.jar input.txt       # command file
java -jar target/amusement-park.jar --fly           # interactive, temporary data
java -jar target/amusement-park.jar --fly input.txt # command file, temporary data
```

Interactive mode reads commands from standard input until `EXIT` or end of input. File mode reads one command per line, ignores blank lines, and stops at `EXIT`.

### Commands

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>   (Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
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
ATTRACTION CREATE;<identifier>;<name>;<capacity>          (GENERAL)
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>   (GENERAL, INDIVIDUAL, GROUP, SPECIAL)
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>   (quantity 1 or 10)
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

Rejected commands print a line starting with `ERROR:` and the application keeps running. Each execution starts with today's date and a base price of 40.00. Group tickets get 0% / 10% / 20% / 50% off per member for fewer than 3 / 3–5 / 6–10 / more than 10 members.

Attraction types: GENERAL accepts individual and group tickets; INDIVIDUAL accepts individual tickets; GROUP accepts group tickets; SPECIAL accepts individual tickets and consumes their Special Pass automatically (no SPEED). With `;SPEED`, one unconsumed SpeedPass per entering ticket is consumed (all or nothing for groups). A SpeedPass costs 20% of its ticket's price, a Special Pass the same as the ticket.

Passwords are stored as BCrypt hashes. User files written by earlier versions (plain-text passwords) are not migrated: those users cannot authenticate.

### Data files

The data directory (the working directory by default) holds:

| File | Line format |
| --- | --- |
| `users.txt` | `FirstName;Surnames;DNI;Email;BCryptHash;Phone;Card` |
| `groups.txt` | `Identifier;Name;DNI1;DNI2;...` |
| `tickets.txt` | `GROUP;id;date;groupId` and `INDIVIDUAL;id;date;basePrice;discount%;groupTicketId;dni;email;firstName;surnames` |
| `attractions.txt` | `Identifier;Name;Capacity;Type` (no type = GENERAL; occupants are not stored) |
| `passes.txt` | `SPEED` or `SPECIAL`, then `;id;individualTicketId;price;consumed` |

Override the directory with `-Dpark.data.dir=<path>`, e.g.
`java -Dpark.data.dir=data -jar target/amusement-park.jar`.

With `--fly`, all files go into a new `tmp/yyyyMMdd_HHmmss/` directory that is deleted when the execution ends.

### Design

Packages: `es.upm.park.presentation` (command processing, help, invoice text), `es.upm.park.business` (model, singleton controllers, invoice builders, exceptions, validation, password hashing) and `es.upm.park.persistence` (repository interfaces and their text-file implementations). `es.upm.park.App` is the composition root: it creates the repositories for the chosen data directory and injects them, and the collaborations between controllers, into the singletons.

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
