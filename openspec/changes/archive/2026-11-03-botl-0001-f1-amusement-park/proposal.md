# Proposal: F1 — Amusement Park, Delivery 1

## Why

The amusement-park assignment needs a verifiable command-line foundation: a Java 21 Maven Quickstart project that packages into an executable JAR and manages persistent users through `;`-separated text commands, from standard input or from a command file. The repository currently contains only OpenSpec scaffolding, so this change delivers the complete first delivery (milestones F0-C01 through F1-C09) as one consolidated change.

## What Changes

- Create a Java 21 Maven project with the standard Maven Quickstart layout (F0-C01).
- Package a standalone executable JAR, started with `java -jar target/nombre-aplicacion.jar [input.txt]` (F0-C02).
- Introduce an encapsulated user with first name, surnames, DNI, email, password, telephone and card number; DNI immutable after creation (F1-C01).
- Validate DNI (eight digits plus correct check letter), email (`text@text.xxx`, three-character extension), password (upper, lower, digit, symbol) and card number (exactly 16 digits), without regular expressions or external libraries (F1-C02).
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, enforcing validation and DNI/email uniqueness (F1-C03).
- Persist users one per line, `;`-separated, in a text file, loaded on start-up, through a class separate from the user and main classes (F1-C04).
- Add `USER DELETE;Email;Password`, deleting only after password verification and updating storage (F1-C05).
- Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD; DNI stays immutable (F1-C06).
- Process commands from standard input or from a file given as the first argument through one shared command pipeline, in order, ignoring blank lines; handle unreadable files in a controlled way (F1-C07).
- Add `HELP` (command syntax) and `EXIT` (controlled termination; no later file line runs) (F1-C08).
- Integrate and verify all delivery-1 behaviour under the delivery's technical restrictions (F1-C09).

## Capabilities

### New Capabilities

- `project-initialization`: Java 21 Maven Quickstart project structure that compiles and tests with Maven.
- `executable-application`: standalone JAR started with `java -jar`, in interactive or file mode.
- `user-domain-model`: user attributes, encapsulation, and DNI immutability.
- `user-data-validation`: DNI, email, password and card-number validation rules.
- `user-creation`: the `USER CREATE` command with validation and uniqueness.
- `user-persistence`: text-file storage of users across runs.
- `user-deletion`: the `USER DELETE` command with credential verification.
- `user-updates`: the `USER UPDATE` command over enum-defined modifiable fields.
- `command-file-execution`: shared processing of interactive and file command input.
- `help-and-exit`: the `HELP` and `EXIT` control commands.
- `delivery-one-integration`: end-to-end delivery-1 behaviour and technical restrictions.

### Modified Capabilities

None (no main specs exist yet).

## Impact

- New `pom.xml`, `src/main/java/**`, `src/test/java/**` in the repository root.
- Runtime: Java standard library only; JUnit 5 is used as a test-scoped dependency only.
- Creates a user data file (`users.txt`) in the working directory at runtime.
- No databases, frameworks, regular expressions or encryption.
