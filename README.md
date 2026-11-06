# caise27 — Amusement Park

Java 21 / Maven console application that manages the users, groups, tickets and attractions of an amusement park.

## Build

```bash
mvn package
```

Produces the executable JAR `target/amusement-park.jar` (standard library only).

## Run

```bash
java -jar target/amusement-park.jar                  # interactive (standard input)
java -jar target/amusement-park.jar input.txt        # command file, one command per line
java -jar target/amusement-park.jar --fly            # interactive, temporary data
java -jar target/amusement-park.jar --fly input.txt  # command file, temporary data
```

Data is stored as text files in the working directory: `users.txt`, `groups.txt`, `tickets.txt`, `attractions.txt` (attraction occupants are not stored).
With `--fly` every file lives in `tmp/<yyyyMMdd_HHmmss>/`, which is deleted when the run ends, so permanent data is never touched.

## Commands

```text
USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>
USER DELETE;<email>;<password>
USER UPDATE;<email>;<password>;<field>;<value>    (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
GROUP CREATE;<id>;<name>
GROUP ADD;<id>;<email>
GROUP REMOVE;<id>;<email>
GROUP DELETE;<id>
TICKET CREATE;<id>;<email>;<yyyy-MM-dd>
TICKET DELETE;<id>
GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>
GROUPTICKET DELETE;<id>
SPEEDPASS CREATE;<purchase>;<ticket>;<quantity>
SPEEDPASS DELETE;<pass>
SPECIALPASS CREATE;<id>;<ticket>
SPECIALPASS DELETE;<id>
ATTRACTION CREATE;<id>;<name>;<capacity>
ATTRACTION LIST;<id>
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION STATUS;<id>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<group>
SYSTEM DATE;<yyyy-MM-dd>
SYSTEM PRICE;<price>
HELP
HELP EXTENDED
EXIT
```

Incorrect commands print an `Error: ...` message and processing continues; `EXIT` stops processing.

## Design

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
