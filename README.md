# Amusement Park

Command-line application for managing an amusement park: users, groups, individual and group tickets, and attractions (Deliveries 1 and 2).

## Requirements

- JDK 21 or newer (the build targets Java 21 with `maven.compiler.release`).
- Maven 3.9 or newer.

The application uses the Java standard library plus jBCrypt (`org.mindrot:jbcrypt:0.4`) for password protection. Maven downloads it, and it is bundled in the executable JAR.

## Build

```text
mvn package
```

This compiles the sources, runs the tests and produces `target/amusement-park.jar`.

## Run

Interactive mode reads commands from standard input, one per line:

```text
java -jar target/amusement-park.jar
```

File mode processes the commands of a text file in order. Blank lines are ignored.

```text
java -jar target/amusement-park.jar input.txt
```

Commands are processed identically in both modes. `EXIT` stops processing, and no later line is executed. A nonexistent or unreadable command file is reported with an `ERROR` line.

## Commands

Elements are separated by `;`. `HELP` prints this list. `HELP EXTENDED` prints it with a short explanation of what each command does.

```text
USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>
USER DELETE;<Email>;<Password>
USER UPDATE;<Email>;<Password>;<Field>;<NewValue>     Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD
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
SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>
SPECIALPASS CREATE;<identifier>;<individualTicket>
SPEEDPASS DELETE;<speedPassIdentifier>
SPECIALPASS DELETE;<specialPassIdentifier>
ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]     type: GENERAL (default), INDIVIDUAL, GROUP, SPECIAL
ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]
ATTRACTION EXIT;<attraction>;<email>
ATTRACTION LIST;<attraction>
ATTRACTION STATUS;<attractionIdentifier>
INVOICE CREATE;USER;<email>
INVOICE CREATE;GROUP;<groupIdentifier>
HELP
HELP EXTENDED
EXIT
```

Park rules in short:

- The system date starts as today, and the base price starts at 40.00 euros on every run. `SYSTEM DATE` and `SYSTEM PRICE` change them.
- A ticket is valid only on its date. Tickets for today or earlier cannot be deleted; they are kept as history.
- A group ticket contains one individual ticket per member, identified `<groupTicket>:<dni>`. Each member's ticket gets a member-count discount: fewer than 3 members, 0%; 3–5, 10%; 6–10, 20%; more than 10, 50%.
- An active group ticket (dated today or later) locks the group against `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE`.
- A user with an active ticket, or in a locked group, cannot be deleted.
- Group entry to an attraction is all-or-nothing within its capacity.
- Attraction types limit the tickets that can enter:
  - `GENERAL`: individual and group tickets.
  - `INDIVIDUAL`: individual tickets only.
  - `GROUP`: group tickets only.
  - `SPECIAL`: individual tickets that have an unconsumed Special Pass. The pass is consumed automatically on entry, and `SPEED` is not allowed.
- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` uses SpeedPasses at `GENERAL`, `INDIVIDUAL` and `GROUP` attractions. Each entering ticket needs an unconsumed SpeedPass, and exactly one is consumed per ticket. A group enters all-or-nothing: if any check fails, nobody enters and no pass is consumed. The response names the consumed passes.
- Passes belong to an individual ticket, including the tickets inside a group ticket, and can be bought for tickets dated today or later.
  - A SpeedPass costs 20% of the ticket's stored price. It is bought 1 or 10 at a time, and the passes are numbered `<purchase>:1` to `<purchase>:10`.
  - A Special Pass costs the ticket's price. A ticket can have only one, even after it has been consumed.
  - Unconsumed passes of tickets dated after the current date can be refunded.
  - Deleting a ticket or group ticket also deletes its passes.
  - Passes are kept in memory and are not saved to files yet; that comes with the Delivery 3 persistence milestone.
- Invoices are generated on demand and never stored. A user invoice lists the user's own individual tickets; tickets that are part of a group ticket are excluded because the group ticket already includes them. A group invoice lists the group's tickets once, at their total price, and shows each ticket's composition as detail. Empty sections are left out.

Successful commands answer with a line starting with `OK`. Rejected or unrecognised commands answer with a line starting with `ERROR` and do not change any data.

Validation rules:

- DNI: eight digits plus the matching check letter.
- Email: `text@text.xxx` with a three-character extension.
- Password: at least one uppercase letter, one lowercase letter, one digit and one symbol.
- Card: exactly 16 digits.
- DNI and email must be unique. The DNI cannot be modified.

## Persistence

Data is stored in text files in the working directory:

| File | Content |
| --- | --- |
| `users.txt` | `FirstName;Surnames;DNI;Email;PasswordHash;Phone;Card` (BCrypt value, never the plain password) |
| `groups.txt` | `id;name;dni1,dni2,...` |
| `tickets.txt` | `I;id;date;price;dni;email;firstName;surnames` and `G;id;date;group;component1,component2,...` |
| `attractions.txt` | `id;name;capacity;type` (users inside are not stored, and attractions start empty; a line without a type is a `GENERAL` attraction) |

Passwords are checked against the validation rules and then stored only as BCrypt values, using the jBCrypt library. `USER DELETE` and `USER UPDATE` check the entered password against the stored value. Users files written by earlier versions with plain-text passwords are not migrated; those lines are ignored with a `WARNING`.

Files are loaded at start-up and rewritten after every successful command. Historical tickets keep their holder data even after the user or group is deleted. Malformed, invalid or duplicated lines are ignored, with a `WARNING`. If a data file exists but cannot be read, the application prints an `ERROR` and processes no command.

### Temporary mode

```text
java -jar target/amusement-park.jar --fly
java -jar target/amusement-park.jar --fly input_fase2.txt
```

With `--fly` as the first argument, all data files are written in a new `tmp/<yyyyMMdd_HHmmss>/` directory, which is deleted when the run ends (`EXIT` or end of input). Normal data files are never touched in this mode.

## Design

The code is split into three layers: `presentation` (commands and responses), `business` (controllers, rules and the domain model in `business.model`), and `persistence` (repositories and data files). The class model is in `uml/model.puml`.

- **Singleton controllers.** `ParkController` and the managers (`UserManager`, `GroupManager`, `TicketManager`, `AttractionManager`, `SystemManager`, `InvoiceManager`) each have one instance, obtained with `getInstance()`.
- **Injected repositories.** Repositories are ordinary, replaceable objects. At start-up, `ParkSession` creates them for the chosen storage location: the working directory, or the temporary directory in `--fly` mode. `ParkConfiguration.initialize` then injects them into `ParkController`, and wires each manager with its collaborators. For example, `AttractionManager` receives `TicketManager`. No dependency injection framework is used.
- **Help.** `HelpGenerator` receives the formatting behaviour (`HelpFormatter`) by injection. `HelpConfiguration` registers `HELP` and `HELP EXTENDED`. A new help variant needs only a new formatter registered under a new command; the command processor does not change.
- **Password protection.** `UserManager` receives a `PasswordProtector` by injection. In normal runs this is `BCryptPasswordProtector`, with cost 10. jBCrypt (`org.mindrot:jbcrypt:0.4`) is the only runtime dependency, and `mvn package` bundles it into `target/amusement-park.jar`.
- **Attraction types.** Ticket capabilities are interfaces: `GeneralAccess`, implemented by every ticket; `IndividualAccess`, implemented by `IndividualTicket`; and `GroupAccess`, implemented by `GroupTicket`. `Attraction<T extends GeneralAccess>` is generic, and each type binds `T` to the capability it accepts. `INDIVIDUAL` and `SPECIAL` both use `IndividualAccess`; the Special Pass check is behaviour of `SpecialAttraction`.
- **Invoices.** Invoices use the Builder pattern. `InvoiceManager` directs `UserInvoiceBuilder` or `GroupInvoiceBuilder`, which extend `InvoiceBuilder`.
