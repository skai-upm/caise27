# Proposal

## Why

The Amusement Park assignment needs a verifiable command-line foundation: a Java 21 Maven Quickstart project packaged as an executable JAR, with persistent user management (create, delete, update) driven by `;`-separated text commands, read either interactively or from a command file. This change delivers milestones F0-C01 through F1-C09 (Delivery 1) as one consolidated OpenSpec change, as instructed by the source proposal.

## What Changes

- Create a Java 21 Maven project following the `maven-archetype-quickstart` standard layout (`pom.xml`, `src/main/java`, `src/test/java`), compilable and testable with `mvn test` / `mvn package` (F0-C01).
- Package an executable JAR at `target/nombre-aplicacion.jar` with a declared main class, runnable with `java -jar` in interactive mode (no arguments) or file mode (first argument is a command file path) (F0-C02).
- Introduce an encapsulated user with first name, surname(s), DNI, email, password, telephone and card number; DNI is immutable after creation (F1-C01).
- Validate DNI (8 digits + correct check letter), email (`text@text.xxx`, three-character extension), password (upper, lower, digit, symbol) and card number (exactly 16 digits) using only basic `String` operations and `split` — no regular expressions or external libraries (F1-C02).
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, validating data and rejecting duplicate DNI or email before any state change (F1-C03).
- Persist users in a text file, one user per line with `;`-separated attributes, loaded at start-up, via a persistence class separate from the user class and the main class; no databases, persistence frameworks or external libraries (F1-C04).
- Add `USER DELETE;Email;Password`: locate by email, verify existence and password, delete, and update persistent data (F1-C05).
- Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`; DNI stays immutable and creation-time validation and uniqueness rules remain in force; changes persist (F1-C06).
- Process commands from standard input or from a file given as first argument through one shared command processor, in order, ignoring blank lines, with controlled handling of nonexistent or unreadable files (F1-C07).
- Add `HELP` (lists available commands and syntax) and `EXIT` (controlled termination; in file mode, no later line is processed) (F1-C08).
- Integrate and verify all Delivery 1 behaviour under the Delivery 1 technical restrictions (Java standard library only; no databases, frameworks, external libraries, regular expressions or encryption) (F1-C09).

## Capabilities

### New Capabilities

- `project-initialization`: Java 21 Maven Quickstart project structure and build (F0-C01).
- `executable-application`: Executable JAR packaging and launch modes (F0-C02).
- `user-domain-model`: User attributes, encapsulation and DNI immutability (F1-C01).
- `user-data-validation`: DNI, email, password and card validation rules (F1-C02).
- `user-creation`: `USER CREATE` command and DNI/email uniqueness (F1-C03).
- `user-persistence`: Text-file storage and start-up loading of users (F1-C04).
- `user-deletion`: `USER DELETE` command with credential verification (F1-C05).
- `user-updates`: `USER UPDATE` command over the modifiable-field enum (F1-C06).
- `command-file-execution`: Shared interactive and file command processing (F1-C07).
- `help-and-exit`: `HELP` and `EXIT` commands (F1-C08).
- `delivery-one-integration`: Delivery 1 technical restrictions and end-to-end behaviour (F1-C09).

### Modified Capabilities

None — the repository has no existing specs or code.

## Impact

- New files: `pom.xml`, Java sources under `src/main/java`, JUnit tests under `src/test/java`.
- Build: Maven with JDK 21; test-scope JUnit dependency only (the Quickstart archetype default). No runtime dependencies.
- Runtime: the application creates/updates a users text file in the working directory.
- Git: the source prescribes 11 separate milestone commits; see design.md for how this interacts with the batch harness that owns Git history for this change.
