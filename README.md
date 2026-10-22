# Amusement Park

Java 21 / Maven command-line application for managing an amusement park: users, groups,
tickets and attractions.

## Build

```text
mvn package
```

This produces the executable JAR `target/amusement-park.jar`.

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

In normal mode data is stored in the working directory: `users.txt`, `groups.txt`,
`tickets.txt` and `attractions.txt`. The system date starts as today and the base ticket
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
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
HELP
EXIT
```

Group tickets get a discount per member count: fewer than 3 → 0 %, 3–5 → 10 %,
6–10 → 20 %, more than 10 → 50 %.

## Design

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
