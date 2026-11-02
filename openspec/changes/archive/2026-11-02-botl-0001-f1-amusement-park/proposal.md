# Proposal

## Why

The amusement-park assignment needs a verifiable command-line foundation: a Java 21 Maven Quickstart project that builds into an executable JAR and manages persistent users through text commands, from interactive standard input or a command file. The repository is currently empty (only OpenSpec scaffolding), so Delivery 1 (milestones F0-C01 through F1-C09) is built here as one consolidated change.

## What Changes

- Create a Java 21 Maven project from the `maven-archetype-quickstart` archetype, with the standard Maven directory layout, at the repository root.
- Package an executable JAR with `mvn package` that starts with `java -jar target/nombre-aplicacion.jar` (interactive mode) or `java -jar target/nombre-aplicacion.jar input.txt` (file mode).
- Introduce an encapsulated user (first name, surnames, DNI, email, password, telephone, card number) whose DNI cannot change after creation.
- Validate DNI (8 digits + correct check letter), email (`text@text.xxx`, three-character extension), password (upper, lower, digit, symbol) and card number (exactly 16 digits) without regular expressions or external libraries.
- Add the `;`-separated commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`, interpreted with basic `String` operations and `split`; invalid commands never terminate the application.
- Enforce DNI and email uniqueness; authenticate by email and password before deletion and modification; restrict modifiable fields to the enum values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`.
- Persist users one per line, `;`-separated, in a text file, loaded at start-up, using a persistence component separate from both the user model and the main class.
- Share one command-processing pathway between interactive and file mode; `EXIT` stops processing, including later lines of a command file.
- Restrict Delivery 1 to the Java standard library at runtime: no databases, frameworks, external libraries, regular expressions or encryption.

## Capabilities

### New Capabilities
- `project-initialization`: Java 21 Maven Quickstart project structure and build baseline.
- `executable-application`: Standalone executable JAR, started with `java -jar`, with interactive and file invocation forms.
- `user-domain-model`: The user and its encapsulated attributes, including DNI immutability.
- `user-data-validation`: DNI, email, password and card-number validation rules.
- `user-creation`: `USER CREATE` command, validation before storage and DNI/email uniqueness.
- `user-persistence`: Text-file storage of users between runs, with separated responsibilities.
- `user-deletion`: `USER DELETE` command with email/password verification.
- `user-updates`: `USER UPDATE` command over enum-defined modifiable fields, preserving validation and uniqueness.
- `command-file-execution`: Interactive and file command input through one shared, ordered processing pathway.
- `help-and-exit`: `HELP` command syntax listing and controlled `EXIT` termination.
- `delivery-one-integration`: Delivery 1 technical restrictions and end-to-end integrated behavior.

### Modified Capabilities

None. `openspec/specs/` is empty.

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**` at the repository root.
- Runtime dependencies: none beyond the JDK 21 standard library. Test scope: JUnit as provided by the Quickstart archetype.
- Runtime data file: a user storage text file created in the working directory when users are first saved.
- Git: the source assignment prescribes 11 milestone commits with exact messages; the batch harness running this change controls Git history (see design.md, "Git milestone history").
