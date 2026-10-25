# caise27 — Amusement Park

Java 21 / Maven command-line application for managing an amusement park:
users, groups, individual and group tickets, and attractions.

## Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`.

## Run

```text
java -jar target/amusement-park.jar                       # interactive
java -jar target/amusement-park.jar input_fase2.txt       # command file
java -jar target/amusement-park.jar --fly                 # interactive, temporary data
java -jar target/amusement-park.jar --fly input_fase2.txt # command file, temporary data
```

In file mode there is one command per line; blank lines are ignored and processing stops at `EXIT`.

Data is stored in `users.txt`, `groups.txt`, `tickets.txt` and `attractions.txt` in the working
directory (another directory can be chosen with `-Dpark.data.dir=<path>`). With `--fly` the same
files are written to a new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the run ends,
so permanent data is neither read nor modified. Attraction occupancy, the system date and the
system price are never persisted.

## Commands

Elements are separated by `;`. Command keywords are case-insensitive.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>
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

`<Field>` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (the DNI cannot be changed).

### Rules

- User data: DNI with a valid control letter, email `text@text.xxx`, password with uppercase,
  lowercase, digit and symbol, card of exactly 16 digits.
- The system date starts as today and the base price as 40.00 EUR in every run.
- A ticket is *active* when its date is on or after the system date. Tickets can only be
  deleted when dated strictly after the system date; older ones are kept as historical records.
- Group tickets contain one individual ticket `<id>:<dni>` per member at purchase time, with a
  discount per ticket: fewer than 3 members 0 %, 3–5 10 %, 6–10 20 %, more than 10 50 %.
- A group with an active group ticket cannot be modified or deleted. A user with an active ticket,
  or belonging to such a group, cannot be deleted.
- `ATTRACTION ENTER` needs a ticket dated exactly the system date; group tickets enter everybody
  or nobody, within the attraction capacity.

## Design

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
