# caise27

## Amusement Park

Java 21 command-line application for managing an amusement park. Delivery 1 covers user management. Delivery 2 adds groups, individual and group tickets, attractions, and a temporary execution mode.

### Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`. The application uses only the Java standard library.

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
ATTRACTION CREATE;<identifier>;<name>;<capacity>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attraction>
HELP
EXIT
```

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

### Data files

Data is stored in the working directory (or the `--fly` temporary directory) as `;`-separated text files:

| File | Content |
| --- | --- |
| `users.txt` | `firstName;surnames;dni;email;password;phone;card` |
| `groups.txt` | `id;name;memberDni;...` |
| `attractions.txt` | `id;name;capacity` (occupants are not stored; attractions start empty) |
| `tickets.txt` | `INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames`, or `GROUP;id;date;groupId;count` followed by `count` `MEMBER;...` lines |

Files are loaded at startup and all rewritten together after every change. Tickets keep their holder's identity, so historical tickets of deleted users or groups survive restarts. Lines that are malformed or invalid are skipped with a warning on standard error.

### Design model

The class model is documented in PlantUML in [uml/model.puml](uml/model.puml). The test `UmlModelTest` checks that it matches the code.
