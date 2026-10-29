# Proposal

## Why

The repository is empty apart from OpenSpec scaffolding, and Amusement Park Delivery 1 requires a working Java 21 Maven application that manages park users through text commands, interactively or from a command file, with file-based persistence. This change covers the full Delivery 1 roadmap (milestones F0-C01 through F1-C09) as one OpenSpec change.

## What Changes

- **F0-C01**: Create a standard Maven project (quickstart archetype layout) compiled for JDK 21.
- **F0-C02**: Configure `mvn package` to produce an executable JAR with a declared entry-point class, runnable with `java -jar target/<jar>.jar` (interactive) and `java -jar target/<jar>.jar input.txt` (file mode).
- **F1-C01**: Add an encapsulated user model (first name, surname(s), DNI, email, password, telephone, card number) with an immutable DNI, plus separate classes for the user, the application entry point, and user storage.
- **F1-C02**: Add validation for DNI (8 digits + correct control letter), email (`text@text.xxx`, String operations and `split` only, no regex), password (uppercase, lowercase, digit, symbol; no regex or libraries) and card number (exactly 16 digits). The telephone number is stored without validation or transformation.
- **F1-C03**: Add the `;`-separated command interface and `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, rejecting invalid data and duplicate DNI or email.
- **F1-C04**: Persist users to a text file, one `;`-separated line per user, loaded at start-up, with reading and writing in a dedicated persistence class.
- **F1-C05**: Add `USER DELETE;Email;Password`, which deletes only on a matching email and password, and updates the file.
- **F1-C06**: Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`). DNI cannot be changed, creation validation and uniqueness rules still apply, and changes are persisted.
- **F1-C07**: Process commands from a file given as the first argument (one per line, in order, blank lines ignored) through the same command processing used for interactive input, and handle a missing or unreadable file in a controlled way.
- **F1-C08**: Add `HELP` (lists command syntax) and `EXIT` (controlled termination; no later command is executed, including in file mode).
- **F1-C09**: Integrate everything so that malformed or unprocessable input never terminates the application unexpectedly, and the project builds from scratch with `mvn package`.
- Restrictions: Java standard library only at runtime (JUnit is used as a test-scope dependency, as the quickstart archetype provides), and no databases, frameworks, regular expressions or encryption.

## Capabilities

### New Capabilities
- `build-packaging`: Maven/JDK 21 project layout and executable JAR packaging and launch modes.
- `user-validation`: Validation rules for DNI, email, password and card number.
- `user-management`: User model invariants and the USER CREATE / USER DELETE / USER UPDATE operations, including uniqueness.
- `user-persistence`: File-based storage and reload of users between runs.
- `command-interface`: Command parsing, interactive and file input modes, HELP, EXIT and controlled error handling.

### Modified Capabilities
<!-- None: there are no existing specs. -->

## Impact

- New `pom.xml` and `src/main/java`, `src/test/java` trees, which make this a Java 21 Maven project.
- Runtime artifact: `target/amusement-park.jar` (see design.md for the JAR name decision).
- A persisted users file is created in the working directory at runtime.
- Git: the source text asks for one commit per milestone with fixed messages. The BOTL batch orchestrator produces one commit per proposal file, so the eleven milestone messages are recorded in that commit's body instead of as separate commits (flagged ambiguity, see design.md).
