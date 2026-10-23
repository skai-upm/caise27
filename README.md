# caise27

## Amusement Park

Java 21 / Maven command-line application for managing an amusement park: users, groups, individual and group tickets, and attractions.

### Build

```text
mvn package
```

This runs the tests and produces the executable JAR `target/amusement-park.jar`.

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
ATTRACTION CREATE;<identifier>;<name>;<capacity>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
HELP
EXIT
```

Rejected commands print a line starting with `ERROR:` and the application keeps running. Each execution starts with today's date and a base price of 40.00. Group tickets get 0% / 10% / 20% / 50% off per member for fewer than 3 / 3–5 / 6–10 / more than 10 members.

### Data files

The data directory (the working directory by default) holds:

| File | Line format |
| --- | --- |
| `users.txt` | `FirstName;Surnames;DNI;Email;Password;Phone;Card` |
| `groups.txt` | `Identifier;Name;DNI1;DNI2;...` |
| `tickets.txt` | `GROUP;id;date;groupId` and `INDIVIDUAL;id;date;basePrice;discount%;groupTicketId;dni;email;firstName;surnames` |
| `attractions.txt` | `Identifier;Name;Capacity` (occupants are not stored) |

Override the directory with `-Dpark.data.dir=<path>`, e.g.
`java -Dpark.data.dir=data -jar target/amusement-park.jar`.

With `--fly`, all files go into a new `tmp/yyyyMMdd_HHmmss/` directory that is deleted when the execution ends.

### Design

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
