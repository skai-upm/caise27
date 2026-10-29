# Proposal

## Why

The repository contains no application yet. Amusement Park Delivery 1 (milestones F0-C01 to F1-C09) requires a Java 21 Maven application that manages park users through text commands, persists them in a text file and runs as an executable JAR both interactively and with a command file. This change delivers the full Delivery 1 baseline that Deliveries 2 and 3 will extend.

## What Changes

- Create a standard Maven project (apache-maven-quickstart layout) targeting JDK 21 that builds with `mvn package` (F0-C01).
- Configure the build to produce an executable JAR with a declared entry point, runnable as `java -jar target/<jar>` with no argument (interactive mode) or with a command file argument (F0-C02).
- Introduce an encapsulated user domain object with first name, surname(s), DNI, email, password, telephone and card number; DNI is immutable after creation (F1-C01).
- Add user data validation for DNI (8 digits + correct control letter), email (`text@text.xxx`, String operations/split, no regex), password (uppercase, lowercase, digit, symbol; no regex/libraries) and card number (exactly 16 digits). Telephone is stored without validation or transformation (F1-C02).
- Add the `USER CREATE` command with validation and DNI/email uniqueness (F1-C03).
- Persist users in a `;`-separated text file (one user per line) through a dedicated persistence class, loaded at start-up (F1-C04).
- Add `USER DELETE;Email;Password` with email lookup, existence and password checks, and persistence update (F1-C05).
- Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD), immutable DNI, re-validation, email uniqueness and persistence (F1-C06).
- Add command-file input sharing the same command processing as interactive mode; blank lines ignored; missing/unreadable file handled in a controlled manner (F1-C07).
- Add `HELP` (lists commands and syntax) and `EXIT` (controlled termination; no later lines processed in file mode) (F1-C08).
- Integrate everything with controlled handling of malformed or unknown input, using the Java standard library only (F1-C09).

## Capabilities

### New Capabilities
- `application-build`: Maven/JDK 21 project structure, `mvn package` build and executable JAR entry point.
- `command-processing`: Text command interpretation, interactive and file execution modes, HELP, EXIT and error tolerance.
- `user-validation`: Validation rules for DNI, email, password and card number.
- `user-management`: User data model and the USER CREATE / USER DELETE / USER UPDATE operations, including uniqueness and DNI immutability.
- `user-persistence`: Storage of users in a `;`-separated text file across application runs.

### Modified Capabilities
- (none — no existing specs)

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Runtime data file `users.txt` (created in the working directory at runtime; not committed).
- No external runtime dependencies; JUnit is a test-scope dependency only.

## Ambiguities flagged (not silently resolved)

1. **Milestone commits vs. one change.** The source asks for one OpenSpec change and one exact-message Git commit per milestone (11 commits). This batch run processes the whole file as a single change and a single commit. The mandatory milestone commit messages cannot be reproduced individually here; they are listed in the commit body for traceability. This is an open issue for the course's history requirement (§15).
2. **JAR name.** `target/nombre-aplicacion.jar` is a placeholder ("application name"). The JAR will be produced as `target/amusement-park.jar`.
3. **Persistence file location/name** is not specified. It will be `users.txt` in the working directory.
4. **Line-wrapped examples** (`Clave1` / `!` on separate lines) are treated as PDF wrapping artifacts of a single line `...;Clave1!;...`.
5. **"Symbol"** in password validation is not defined; it is interpreted as any character that is neither a letter, a digit nor whitespace.
6. **Output messages** are not contractual (§16); success messages follow the example (`USER CREATED`, `USER UPDATED`, `USER DELETED`); errors are reported as `ERROR: <reason>`.
7. No validation is specified for first name, surname(s) or telephone; none is added (values cannot contain `;` because it is the command separator).
8. **Missing command file** handling is only required to be "controlled": the application reports an error and terminates with a non-zero exit status without processing anything.
