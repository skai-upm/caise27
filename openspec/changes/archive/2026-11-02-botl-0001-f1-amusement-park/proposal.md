# Proposal

## Why

The repository is empty apart from OpenSpec scaffolding. The amusement-park assignment needs a verifiable command-line foundation for Delivery 1: a standard Java 21 / Maven Quickstart project packaged as an executable JAR, with persistent user management (create, delete, update) and one shared command pipeline for interactive and file input. Later deliveries (F2, F3) build on this.

## What Changes

- Create a Maven Quickstart-layout project targeting Java 21 that builds with `mvn package` and produces `target/nombre-aplicacion.jar`, runnable with `java -jar` with no arguments (interactive) or with a command-file path (file mode). (F0-C01, F0-C02)
- Introduce an encapsulated user domain object (first name, surnames, DNI, email, password, telephone, card number) whose DNI cannot change after creation. (F1-C01)
- Add validation for DNI (8 digits + check letter from the standard Spanish algorithm), email (`text@text.xxx`, extension exactly three characters), password (at least one uppercase, lowercase, digit and symbol) and card number (exactly 16 digits), implemented with plain String operations only: no regular expressions, no external libraries. (F1-C02)
- Add the commands `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, `USER DELETE;Email;Password` and `USER UPDATE;Email;Password;Field;NewValue`. The commands are parsed with basic String operations and `split`, enforce DNI and email uniqueness, and authenticate deletion and update. Updatable fields come from an enum: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD. (F1-C03, F1-C05, F1-C06)
- Persist users in a semicolon-separated text file, one user per line, using a persistence class separate from the user class and the main class. Users load at start-up and are saved after every successful mutation. (F1-C04)
- Process standard input or a file given as the first argument through one shared parser and executor. File commands run in order, blank lines are ignored, and an unreadable or missing file is handled in a controlled way. (F1-C07)
- Add `HELP`, which lists the command syntax, and `EXIT`, which stops processing in both modes so no later file line runs. (F1-C08)
- Integrate and verify all Delivery 1 behavior end to end with the standard library only, and keep the executable JAR. (F1-C09)

## Capabilities

### New Capabilities

- `project-initialization`: Java 21 Maven Quickstart project structure that compiles and tests with Maven.
- `executable-application`: Standalone executable JAR started with `java -jar`, interactive or with a command file argument.
- `user-domain-model`: Encapsulated user with the seven required attributes and an immutable DNI.
- `user-data-validation`: Rules for DNI, email, password and card number, with no regex or external libraries.
- `user-creation`: `USER CREATE` command with validation and DNI/email uniqueness.
- `user-persistence`: One-line-per-user semicolon text file loaded at start-up and kept up to date.
- `user-deletion`: `USER DELETE` with email lookup and password verification.
- `user-updates`: `USER UPDATE` of enum-defined fields with authentication, validation and uniqueness, keeping DNI immutable.
- `command-file-execution`: Shared command processing for standard input and command files.
- `help-and-exit`: `HELP` syntax listing and `EXIT` controlled termination.
- `delivery-one-integration`: Delivery 1 technical restrictions and end-to-end robustness.

### Modified Capabilities

(none: no main specs exist yet)

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**` (Maven standard layout).
- Runtime dependencies: none (Java standard library only). Test scope: JUnit 5, used only for automated verification, as the Quickstart archetype does.
- Runtime data file: `users.txt` in the working directory, created on the first successful mutation.
- Git: the source prescribes 11 milestone commits. Git history in this repository is owned by the BOTL batch harness (one commit per proposal), so see design.md for how this conflict is resolved.
