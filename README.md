# caise27

Amusement park management application (Java 21, Maven; the only runtime library is jBCrypt for
password protection).

## Architecture

- `es.upm.amusementpark.presentation` — `Main`, command parsing and output (`CommandProcessor`), help generation.
- `es.upm.amusementpark.business` — domain model, singleton controllers, invoice Builder, password hashing,
  exceptions; `ApplicationInitializer` creates the repositories and injects them into the controllers.
- `es.upm.amusementpark.persistence` — repositories and text-file stores.

## Build

```text
mvn package
```

This produces the executable JAR `target/nombre-aplicacion.jar`.

## Run

Interactive mode (commands read from standard input):

```text
java -jar target/nombre-aplicacion.jar
```

File mode (one command per line; blank lines are ignored; processing stops at `EXIT`):

```text
java -jar target/nombre-aplicacion.jar input.txt
```

Temporary mode (`--fly`, interactive or with a file): all data is kept in `tmp/<yyyyMMdd_HHmmss>/`,
which is deleted when the execution ends.

```text
java -jar target/nombre-aplicacion.jar --fly
java -jar target/nombre-aplicacion.jar --fly input_fase2.txt
```

Park data is stored in `users.txt` (passwords as BCrypt values), `groups.txt`, `tickets.txt`, `passes.txt`
and `attractions.txt` in the working directory and loaded at start-up. Attractions always start empty; the system date starts as today and
the base price as 40.00 euros in every execution.

## Commands

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
ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>      (type: GENERAL, INDIVIDUAL, GROUP, SPECIAL)
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION ENTER;<attraction>;<ticket>;SPEED
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>   (quantity: 1 or 10)
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPECIALPASS DELETE;<specialPassIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

The class model is documented in `uml/model.puml`.
