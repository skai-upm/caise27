# Amusement Park

Command-line application for managing an amusement park: users, groups, tickets, group tickets, typed attractions, SpeedPasses, Special Passes and invoices (Delivery 3).

## Requirements

- JDK 21 (or newer, compiling for release 21)
- Maven 3

## Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar` (it bundles the jBCrypt library used to protect passwords).

## Run

```text
java -jar target/amusement-park.jar                     # interactive (standard input)
java -jar target/amusement-park.jar input.txt           # commands from a file, in order
java -jar target/amusement-park.jar --fly               # interactive, temporary data
java -jar target/amusement-park.jar --fly input.txt     # file, temporary data
```

Data is stored in the working directory in `users.txt` (passwords as BCrypt hashes), `groups.txt`, `tickets.txt`, `attractions.txt` and `passes.txt` and loaded again on the next run (attractions always start empty). With `--fly` all data files live in `tmp/<yyyyMMdd_HHmmss>/`, which is deleted when the execution ends.

At every start the system date is today and the base ticket price is 40.00 EUR; both can be changed for the current execution with `SYSTEM DATE` and `SYSTEM PRICE`.

## Commands

Elements of a command are separated by `;`.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>      Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
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
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>   type: GENERAL, INDIVIDUAL, GROUP, SPECIAL
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>   quantity: 1 or 10
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

Group tickets get a discount per member depending on the group size: fewer than 3 members 0%, 3-5 10%, 6-10 20%, more than 10 50%. Tickets dated today or earlier cannot be deleted. A group with a group ticket dated today or later is locked (no ADD/REMOVE/DELETE).

Attraction types: GENERAL accepts individual and group tickets; INDIVIDUAL only individual tickets; GROUP only group tickets; SPECIAL only individual tickets with an unconsumed Special Pass (used automatically on entry; no SpeedPasses). A SpeedPass costs 20% of its ticket price, a Special Pass 100% (at most one per ticket). `;SPEED` consumes one SpeedPass per individual ticket; group entry with SpeedPasses is all-or-nothing. Unconsumed passes of tickets dated after today can be refunded; deleting a future ticket also deletes its passes.

Invalid commands or data are reported with a line starting with `ERROR:`; the application keeps running. After `EXIT` no further command is processed.

## Design

Packages: `presentation` (entry point, command processing, help, invoice formatting), `business` (model, services/controllers, invoice builders, exceptions, composition root) and `persistence` (repositories and text-file storage). Controllers are singletons configured by `ParkInitializer`, which creates and injects the repositories for normal or `--fly` mode.

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
