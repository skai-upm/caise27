# caise27 — Amusement Park

Java 21 / Maven command-line application for managing an amusement park:
users, groups, individual and group tickets, SpeedPasses and Special Passes,
typed attractions and invoices.

## Build

```text
mvn package
```

This produces the self-contained executable JAR `target/amusement-park.jar`
(the only external dependency, jBCrypt, is bundled into it).

## Run

```text
java -jar target/amusement-park.jar                       # interactive
java -jar target/amusement-park.jar input_fase3.txt       # command file
java -jar target/amusement-park.jar --fly                 # interactive, temporary data
java -jar target/amusement-park.jar --fly input_fase3.txt # command file, temporary data
```

In file mode there is one command per line; blank lines are ignored and processing stops at `EXIT`.

Data is stored in `users.txt`, `groups.txt`, `tickets.txt`, `passes.txt` and `attractions.txt` in
the working directory (another directory can be chosen with `-Dpark.data.dir=<path>`). With `--fly`
the same repositories are configured on a new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted
when the run ends, so permanent data is neither read nor modified. Attraction occupancy, the system
date and the system price are never persisted.

Passwords are stored only as BCrypt hashes. User files written by Delivery 1/2 (plain-text
passwords) are not migrated: those users cannot authenticate and must be re-created.

## Commands

Elements are separated by `;`. Command keywords are case-insensitive. `HELP EXTENDED` prints a
description of every command.

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
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

### Rules

- User data: DNI with a valid control letter, email `text@text.xxx`, password with uppercase,
  lowercase, digit and symbol, card of exactly 16 digits. `<Field>` is one of `NAME`, `SURNAME`,
  `EMAIL`, `PASSWORD`, `PHONE`, `CARD`.
- The system date starts as today and the base price as 40.00 EUR in every run.
- A ticket is *active* when its date is on or after the system date. Tickets can only be deleted
  when dated strictly after the system date; deleting a ticket also deletes all its passes.
- Group tickets contain one individual ticket `<id>:<dni>` per member at purchase time, with a
  discount per ticket: fewer than 3 members 0 %, 3–5 10 %, 6–10 20 %, more than 10 50 %.
- A group with an active group ticket cannot be modified or deleted. A user with an active ticket,
  or belonging to such a group, cannot be deleted.
- SpeedPasses are bought 1 or 10 at a time (`<purchaseId>:1..n`), each costing 20 % of the
  ticket's price. A ticket can have at most one Special Pass (same price as the ticket). Only
  unconsumed passes of future tickets can be refunded; consumed passes are kept as records.
- Attraction types: `GENERAL` (individual and group tickets), `INDIVIDUAL` (individual tickets),
  `GROUP` (group tickets), `SPECIAL` (individual tickets with an unconsumed Special Pass, consumed
  automatically; no SpeedPass). With `;SPEED` one SpeedPass per entering individual ticket is
  consumed; group entry is all or nothing.
- Invoices: a user invoice lists directly bought tickets and the user's passes (also those on
  tickets inside group tickets); a group invoice lists its group tickets with their composition.

## Design

Packages: `presentation` (entry point, composition root, command processing, help),
`business` (`model`, `controller` singletons, `invoice` builders, `exception`, `security`,
`validation`) and `persistence` (repository interfaces and text-file implementations).
The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
