# caise27

Amusement Park application (Java, Maven).

## Prerequisites

- JDK 21 or newer (the build compiles with `--release 21`)
- Apache Maven 3.9+

## Build and test

```bash
mvn clean test
```

## Packaging

```bash
mvn package
```

This produces the executable JAR `target/amusement-park.jar`. Its only third-party runtime library is jBCrypt (`org.mindrot:jbcrypt:0.4`), which `maven-shade-plugin` bundles into the JAR so that `java -jar` needs no extra classpath.

## Running

Interactive mode reads commands from standard input, one per line:

```bash
java -jar target/amusement-park.jar
```

File mode runs the commands of a text file in order:

```bash
java -jar target/amusement-park.jar input.txt
```

Temporary mode runs with an isolated, throw-away data directory:

```bash
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input.txt
```

Blank lines are ignored. Unknown commands and commands that fail are reported with an `ERROR:` message, and processing continues. A command file that does not exist or cannot be read is reported, and no command runs.

General commands:

- `HELP` lists the available commands and their syntax.
- `HELP EXTENDED` lists every command with its syntax and a short explanation of its purpose. Help text is produced by a `HelpGenerator` with an injected `HelpFormatter`, so new help formats can be added without touching command processing.
- `EXIT` ends the application. No command after `EXIT` is executed, including later lines of a command file.

User commands:

- `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>` registers a user after validating the data and checking that the DNI and email are not already in use. The password is stored BCrypt-protected.
- `USER DELETE;<Email>;<Password>` deletes a user after verifying the password. It is rejected while the user has a ticket dated today or later or belongs to a group with an active group ticket. Otherwise the user is removed from their groups, and historical tickets are kept.
- `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>` changes one field (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) after verifying the password. The DNI cannot be changed, and new values follow the creation rules. After an email change, the user's groups, tickets, passes and attraction occupancy follow the new email.

System commands (park-wide settings for the current execution):

- `SYSTEM DATE;<yyyy-MM-dd>` sets the current system date. It starts as the local date when the application starts.
- `SYSTEM PRICE;<Price>` sets the current base ticket price, a positive amount with at most two decimals. It starts at 40.00 euros on every execution.

Group commands:

- `GROUP CREATE;<Identifier>;<Name>` creates an empty group with a unique identifier.
- `GROUP ADD;<Identifier>;<Email>` adds a registered user to a group (at most once).
- `GROUP REMOVE;<Identifier>;<Email>` removes a member from a group.
- `GROUP DELETE;<Identifier>` deletes a group.

A group with a group ticket dated today or later cannot be modified or deleted.

Ticket commands:

- `TICKET CREATE;<identifier>;<email>;<date>` buys an individual ticket for a registered user, dated today or later, at the current base price. The price is displayed.
- `TICKET DELETE;<identifier>` deletes an individual ticket dated strictly after the current date. Tickets for today or earlier are kept as historical records.

A user with a ticket dated today or later, or who belongs to a group with an active group ticket, cannot be deleted.

Group ticket commands:

- `GROUPTICKET CREATE;<identifier>;<group>;<date>` buys a joint ticket for all current members of a non-empty group. It contains one individual ticket per member, identified `<identifier>:<dni>`. The total price is displayed.
- `GROUPTICKET DELETE;<identifier>` deletes a group ticket dated strictly after the current date, together with its individual tickets.

Group discount on the base price of each member's ticket: fewer than 3 members 0%, 3-5 members 10%, 6-10 members 20%, more than 10 members 50%. Individual tickets that belong to a group ticket cannot be deleted with `TICKET DELETE`.

Attraction commands:

- `ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>[;<Type>]` creates an attraction with a positive maximum capacity. The type is `GENERAL` (default, individual and group tickets), `INDIVIDUAL` (individual tickets only), `GROUP` (group tickets only) or `SPECIAL` (individual tickets with an unconsumed Special Pass). Types are generic attractions bounded by ticket access capabilities (`AccessTicket`, `IndividualAccess`, `GroupAccess`).
- `ATTRACTION LIST;<AttractionIdentifier>` shows the emails of the users currently inside it (read-only).
- `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>` admits the ticket's user(s) when the ticket is dated exactly the current system date and the attraction has room for all of them. The date is checked only at entry.
- With a group ticket, all its users enter together or nobody enters.
- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` uses one unconsumed SpeedPass per individual ticket (each member of a group ticket needs one). It is not allowed at SPECIAL attractions, which automatically consume the ticket's Special Pass instead. Entry and pass consumption happen together or not at all, and the output names the consumed passes.
- `ATTRACTION EXIT;<AttractionIdentifier>;<Email>` lets a user leave and frees their place.
- `ATTRACTION STATUS;<AttractionIdentifier>` shows the identifier, name, capacity, number of users inside, available places and the users inside.

Pass commands (personal extras bought for an individual ticket, also for tickets in a group ticket):

- `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>` buys 1 or 10 SpeedPasses (`<purchaseIdentifier>:1..n`), each costing 20% of the ticket's price. The total is displayed.
- `SPECIALPASS CREATE;<identifier>;<individualTicket>` buys the ticket's only Special Pass, at the ticket's price.
- `SPEEDPASS DELETE;<speedPassIdentifier>` and `SPECIALPASS DELETE;<specialPassIdentifier>` refund an unconsumed pass whose ticket is dated after today.

Deleting a ticket or group ticket also refunds its passes. User invoices include the user's passes.

Invoice commands:

- `INVOICE CREATE;USER;<email>` shows the user's identification, the individual tickets bought directly for them, and the total. Tickets that are part of a group ticket are not counted again.
- `INVOICE CREATE;GROUP;<groupIdentifier>` shows the group, its group tickets with their composition, and the total of the group tickets.

Invoices are built with the Builder pattern (`business.invoice`), and empty sections are omitted.

## Persistence

Users (`users.txt`, one per line: `FirstName;Surnames;DNI;Email;Password;Phone;Card`, with the password BCrypt-protected), groups, tickets (including group tickets and their composition), attractions (with their type) and passes (with their ticket, price and consumed state) are stored as text files in `data/` and reloaded at startup. Attractions always start empty. Consumed passes are kept while their ticket exists. Refunds and ticket deletions remove the affected passes. Historical tickets are kept even after their user or group is deleted. With `--fly`, all files go to a new `tmp/<yyyyMMdd_HHmmss>/` directory, which is deleted when the execution ends.

## Design model

The PlantUML class model is in [`uml/model.puml`](uml/model.puml). `UmlModelConsistencyTest` fails if a type of the model, controller or persistence packages is missing from it.

## Delivery 2 example session

```text
SYSTEM DATE;2027-04-15
GROUP CREATE;G01;Familia Garcia
ATTRACTION CREATE;A01;Dragon Rojo;20
GROUPTICKET CREATE;TG01;G01;2027-04-15
ATTRACTION ENTER;A01;TG01
ATTRACTION STATUS;A01
EXIT
```

Save it as `input_fase2.txt` and run `java -jar target/amusement-park.jar --fly input_fase2.txt` to try it without changing `data/`. Group members and ticket owners must be registered users.

## Architecture

The code is organized in layers under `es.upm.park`:

- `presentation`: command parsing and processing, the command loop and command-file reading.
- `business`: `model` (domain objects), `controller` (central park management and its storage/user ports) and `exception` (the `ParkException` hierarchy: not found, business rule, invalid input, storage).
- `persistence`: text-file repositories that implement the storage port.
- `App` and `AppConfiguration`: the composition root. `App` parses arguments. `AppConfiguration` creates the repositories for the data directory (`data/` or the `--fly` temporary one), injects them into the storage and the controllers, wires `InvoiceController` to `ParkController`, and creates the command processor. Injection is plain Java, with no framework.

Controllers with an execution-wide responsibility (`ParkController`, `InvoiceController`) are singletons. Repositories are not: each configuration creates its own.

Presentation never uses persistence, and business uses neither (`LayeredArchitectureTest`). Every abnormal situation is an exception, which the presentation layer reports as `ERROR:` before continuing with the next command.

## Users

A user (`business.model.User`) has a first name, surnames, DNI, email, password (stored protected value), phone and card number. All fields are private, and the DNI cannot change after creation. `business.controller.UserRegistry` stores and retrieves users by email and DNI, and serves as the park controller's `UserDirectory`.

User data is validated by `business.validation.UserValidator` using plain String operations:

- DNI: 8 digits and the correct check letter (`12345678Z`).
- Email: `text@text.xxx`, with a three-character extension.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
- Card: exactly 16 digits.
- Phone: stored as given.

## Passwords

User passwords are never stored in plain text. `business.security.PasswordService` validates a new or changed password, protects it with BCrypt (salted, `$2a$...`) before it is stored, and verifies entered passwords against the stored value. The hashing algorithm and the validation policy (the password rule of `UserValidator`) are injected by `AppConfiguration`.

## Delivery 3 example session

```text
SYSTEM DATE;2027-04-15
ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL
ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL
TICKET CREATE;T001;ana@correo.com;2027-04-15
SPEEDPASS CREATE;SP01;T001;10
SPECIALPASS CREATE;PE01;T001
ATTRACTION ENTER;A02;T001;SPEED
ATTRACTION ENTER;A04;T001
INVOICE CREATE;USER;ana@correo.com
HELP EXTENDED
EXIT
```

Run it with `java -jar target/amusement-park.jar --fly input_fase3.txt`. Ticket owners must be registered users.
