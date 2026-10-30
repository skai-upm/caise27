# caise27

## Amusement Park

Command-line application (Java 21, Maven) that manages an amusement park: users, groups, individual and group
tickets, SpeedPasses and Special Passes, typed attractions and invoices.

### Build

```text
mvn package
```

This runs the tests and produces `target/amusement-park.jar` (it bundles jBCrypt, the only runtime dependency, used to
protect passwords).

### Run

Interactive mode (commands are read from standard input until `EXIT`):

```text
java -jar target/amusement-park.jar
```

File mode (one command per line, blank lines ignored, processing stops at `EXIT`):

```text
java -jar target/amusement-park.jar input.txt
```

Temporary mode (`--fly`, interactive or with a command file): all data is kept in a new `tmp/<yyyyMMdd_HHmmss>/`
directory that is deleted when the execution ends, so permanent data is never touched:

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input.txt
```

### Commands

Elements are separated by `;`.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>   Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
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

`HELP EXTENDED` shows a description of every command.

Rejected commands print a line starting with `ERROR:` and the application keeps running.

- The system date starts as today's date and the base price as 40.00 euros in every execution.
- Group tickets get a discount per member: fewer than 3 → 0%, 3-5 → 10%, 6-10 → 20%, more than 10 → 50%.
- Tickets and group tickets can only be deleted when dated strictly after the current date.
- Groups with an active group ticket (dated today or later) cannot be modified or deleted.
- A SpeedPass costs 20% of its ticket's price, a Special Pass the same as the ticket (at most one per ticket).
  Unconsumed passes of future tickets can be refunded; deleting a ticket also deletes its passes.
- GENERAL attractions accept any ticket, INDIVIDUAL only individual tickets, GROUP only group tickets, and SPECIAL
  only individual tickets with an unconsumed Special Pass (consumed automatically on entry, no SPEED).

### Data

Data is stored in the working directory: `users.txt` (`FirstName;Surnames;DNI;Email;BCryptHash;Phone;Card`),
`groups.txt`, `tickets.txt`, `attractions.txt` and `passes.txt`. Attraction occupants are not stored. Users files
from earlier versions with plain-text passwords are not migrated.

### Design

Code is layered in packages under `es.upm.amusementpark`: `presentation` (command processing, help), `business`
(model, Singleton controllers, invoices with the Builder pattern, exceptions, password security) and `persistence`
(repository interfaces and their text-file implementations). `ApplicationInitializer` creates the repositories for
the data directory (normal or `--fly`) and injects them into the controllers.

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
