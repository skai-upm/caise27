# Proposal

## Why

The repository is empty apart from OpenSpec scaffolding. Delivery 1 of the Amusement Park assignment requires the foundation of a Java 21 / Maven application: an executable JAR that processes text commands interactively or from a file, and a complete, validated, persistent user-management feature that later deliveries (groups, tickets, attractions) will build upon.

## What Changes

- Create a standard Maven project (quickstart layout) targeting Java 21 that builds with `mvn package` and produces an executable JAR runnable with `java -jar target/<app>.jar [input.txt]`.
- Add a command-based interface: commands are `;`-separated text parsed with basic `String` operations and `split` (no regex, no external libraries). Initial commands: `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`. Invalid input never terminates the application unexpectedly.
- Add two execution modes sharing one command processor: interactive (standard input) and file mode (first argument is a command file; blank lines ignored; processing stops at `EXIT`; missing/unreadable file handled in a controlled manner).
- Add a `User` domain model (first name, surnames, DNI, email, password, phone, card) with encapsulated attributes and an immutable DNI.
- Add user data validation without regular expressions: Spanish DNI letter algorithm, email `text@text.xxx` with a 3-character extension, password with uppercase/lowercase/digit/symbol, card of exactly 16 digits.
- Add user operations: create (with DNI and email uniqueness), delete (email + password check), update (email + password check; modifiable fields given by an enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; DNI immutable; new values re-validated and uniqueness enforced).
- Add text-file persistence (one user per line, `;`-separated) in a dedicated class; users are loaded at start-up and the file is rewritten after every successful mutating operation.

## Capabilities

### New Capabilities
- `application-execution`: Maven build and executable JAR, interactive and file execution modes, command parsing and dispatch, `HELP` and `EXIT`, controlled handling of invalid input.
- `user-management`: The user model and the `USER CREATE`, `USER DELETE` and `USER UPDATE` operations, including uniqueness of DNI and email and DNI immutability.
- `user-validation`: Validation rules for DNI, email address, password and card number, reusable by every user operation.
- `user-persistence`: Storage of users in a `;`-separated text file and their reload across application runs.

### Modified Capabilities
<!-- None: no existing specs. -->

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**` (JUnit tests), `.gitignore` entries for build output and the runtime user data file.
- No external runtime dependencies (Java standard library only); JUnit is a test-scope dependency only.
- Runtime creates a user data file (default `users.txt` in the working directory).
- Out of scope: the course's mandatory per-milestone Git commit roadmap (`[F0-C01]` … `[F1-C09]`). This batch workflow records the delivery as a single traceable commit; the milestone structure is reflected in `tasks.md` instead.
