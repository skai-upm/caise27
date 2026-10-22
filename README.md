# Amusement Park

Java 21 / Maven command-line application for managing an amusement park: users, groups,
tickets, passes, typed attractions and invoices.

## Build

```text
mvn package
```

This produces the self-contained executable JAR `target/amusement-park.jar` (it bundles
the jBCrypt library used to protect passwords).

## Run

Interactive mode (commands are read from standard input):

```text
java -jar target/amusement-park.jar
```

Command file mode (one command per line, blank lines ignored):

```text
java -jar target/amusement-park.jar input.txt
```

Temporary mode (`--fly`, optionally followed by a command file): all data is kept in a
new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the execution ends.

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input.txt
```

In normal mode data is stored in the working directory: `users.txt` (passwords as BCrypt
hashes), `groups.txt`, `tickets.txt`, `attractions.txt` and `passes.txt`. The system date starts as today and the base ticket
price as 40.00 EUR on every execution; neither is stored.

## Commands

Elements are separated by `;`.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>   Field: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD
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
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>    type: GENERAL | INDIVIDUAL | GROUP | SPECIAL
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>   quantity: 1 | 10
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

Attraction types: GENERAL accepts individual and group tickets; INDIVIDUAL only individual
tickets; GROUP only group tickets; SPECIAL only individual tickets with an unconsumed Special
Pass (consumed automatically, no SPEED). With `SPEED`, one unconsumed SpeedPass per admitted
ticket is consumed. A SpeedPass costs 20 % of its ticket's price; a Special Pass the same as
the ticket.

Group tickets get a discount per member count: fewer than 3 → 0 %, 3–5 → 10 %,
6–10 → 20 %, more than 10 → 50 %.

## Design

Packages: `presentation` (command processing, help, invoice printing), `business`
(model, singleton controllers, invoice builders, exceptions, validation, password hashing)
and `persistence` (repository interfaces and their file implementations). `App` is the
composition root that creates the repositories and injects them into the controllers.

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
