# Proposal

## Why

The repository is empty apart from OpenSpec scaffolding. Delivery 1 of the Amusement Park assignment asks for the foundation of a Java 21 / Maven application: a buildable, executable JAR that reads text commands (interactively or from a file) and manages park users (create, update, delete) with validation and file-based persistence. Later deliveries will add park-specific features on top of this foundation, so the command loop, user model and persistence must be cleanly separated now.

## What Changes

- Create a standard Maven project (quickstart archetype structure) targeting Java 21 that builds with `mvn package` into an executable JAR (`target/amusement-park.jar`).
- Add a command-based text interface: `;`-separated commands parsed with basic `String` operations and `split`, no regular expressions or external libraries. Initial commands: `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`. Invalid input never crashes the application.
- Add two execution modes sharing one command processor: interactive (no argument, reads standard input) and file mode (first argument is a command file; blank lines ignored; processing stops at `EXIT`; missing/unreadable files handled in a controlled way).
- Add a `User` domain model (first name, surnames, DNI, email, password, phone, card) with encapsulated attributes and an immutable DNI.
- Add user data validation without regular expressions: Spanish DNI checksum letter, `text@text.xxx` email format, password character classes, 16-digit card number.
- Enforce uniqueness of DNI and email on creation and on email update.
- Add `USER DELETE` (email + password authentication) and `USER UPDATE` (email + password authentication, modifiable fields defined by an enum: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD; DNI not modifiable; same validation rules as creation).
- Persist users to a plain text file (one user per line, `;`-separated attributes), loaded at start-up and rewritten after every successful mutation, implemented in a class separate from the user model and the main class.

Out of scope for this change: the assignment's mandatory GitLab milestone commit messages (`[F0-C01]` … `[F1-C09]`) and submission logistics. Git history for this repository is produced by the batch orchestrator (one commit per proposal), so the milestone roadmap is a course-process requirement that is not reproduced here; the functional state required by milestone F1-C09 is what this change delivers.

## Capabilities

### New Capabilities
- `application-runtime`: Building the project into an executable JAR and running it in interactive or file mode, including controlled handling of missing command files.
- `command-interface`: Parsing `;`-separated text commands, dispatching them, the `HELP` and `EXIT` commands, and controlled handling of unknown or malformed commands.
- `user-management`: The user attributes, validation rules (DNI, email, password, card), uniqueness, and the `USER CREATE` / `USER UPDATE` / `USER DELETE` operations.
- `user-persistence`: Storing users in a text file between runs and loading them at start-up.

### Modified Capabilities
- None.

## Impact

- New files: `pom.xml`, `src/main/java/...` (application, command processing, user model, validation, persistence), `src/test/java/...` (JUnit tests), and a README section on building and running.
- Build dependency: JUnit Jupiter in `test` scope only (provided by the quickstart archetype); no runtime dependencies beyond the Java standard library.
- Runtime data file: `users.txt` in the working directory (path overridable via a system property for testing).
