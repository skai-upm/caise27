# caise27

## Amusement Park

Command-line application (Java 21, Maven) that manages an amusement park: users, groups, individual and group
tickets, and attractions.

### Build

```text
mvn package
```

This runs the tests and produces `target/amusement-park.jar`.

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
ATTRACTION ENTER;<attraction>;<ticket>
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
HELP
EXIT
```

Rejected commands print a line starting with `ERROR:` and the application keeps running.

- The system date starts as today's date and the base price as 40.00 euros in every execution.
- Group tickets get a discount per member: fewer than 3 → 0%, 3-5 → 10%, 6-10 → 20%, more than 10 → 50%.
- Tickets and group tickets can only be deleted when dated strictly after the current date.
- Groups with an active group ticket (dated today or later) cannot be modified or deleted.

### Data

Data is stored in the working directory: `users.txt` (`FirstName;Surnames;DNI;Email;Password;Phone;Card`),
`groups.txt`, `tickets.txt` and `attractions.txt`. Attraction occupants are not stored.

### Design

The class model is documented in [`uml/model.puml`](uml/model.puml) (PlantUML).
