# Proposal

## Why

The repository is empty apart from OpenSpec scaffolding. Delivery 1 of the Amusement Park assignment (milestones F0-C01 to F1-C09) requires a Maven/JDK 21 application, packaged as an executable JAR, that manages park users via text commands (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), validates user data, enforces DNI/email uniqueness and persists users in a text file between runs, in both interactive and command-file modes.

Source: `.botl/proposals/opus_5_setup_single_gpt/0001-1.F1-Amusement-Park/1.F1-Amusement-Park.md` (sections 2.1, 2.2, 3.1, 3.2, 4–13, 15–17 and milestones 14.1–14.11).

## What Changes

- **F0-C01** Create a standard Maven project (quickstart archetype layout) configured for JDK 21, compilable with Maven.
- **F0-C02** Configure the build so `mvn package` produces `target/nombre-aplicacion.jar`, runnable with `java -jar` with a declared entry point.
- **F1-C01** Introduce an encapsulated user model (first name, surnames, DNI, email, password, phone, card) whose DNI is immutable, plus separate classes for running the application and storing/retrieving users.
- **F1-C02** Add validation of DNI (Spanish check-letter algorithm), email (String operations and `split`, three-character extension), password (upper, lower, digit, symbol) and card number (exactly 16 digits), without regular expressions or external libraries.
- **F1-C03** Add `;`-separated command parsing with `USER CREATE`, validating data and enforcing DNI and email uniqueness.
- **F1-C04** Persist users in a text file (one user per line, `;`-separated attributes) loaded at startup, implemented in a dedicated persistence class.
- **F1-C05** Add `USER DELETE;Email;Password` (locate by email, verify existence and password, delete, persist).
- **F1-C06** Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`), DNI immutable, same validation and uniqueness rules, persisted.
- **F1-C07** Accept a command file path as first argument; process its lines in order, ignoring blank lines, through the same command processing as interactive mode; handle missing/unreadable files in a controlled way.
- **F1-C08** Add `HELP` (lists commands and syntax) and `EXIT` (controlled termination; no further commands processed, including in file mode).
- **F1-C09** Integrate everything: invalid input never terminates the application unexpectedly; build from scratch with `mvn package`; JAR runs without an IDE in both modes.

## Capabilities

### New Capabilities
- `application-packaging`: Maven/JDK 21 build, executable JAR, entry point and the technical restrictions of Delivery 1 (standard library only, no regex, no databases/frameworks).
- `user-data-validation`: Rules that decide whether DNI, email, password and card number values are acceptable.
- `user-management`: The user concept and the create, delete and update operations, including uniqueness and DNI immutability.
- `user-persistence`: Storage of users in a text file between runs and loading at startup.
- `command-interface`: Text command parsing, interactive and file input modes, `HELP` and `EXIT`, and controlled handling of invalid input.

### Modified Capabilities
- None (no existing specs).

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Runtime dependencies: none beyond the Java standard library. Test-scope dependency: JUnit Jupiter (test only, not packaged into the JAR).
- Runtime data file: `users.txt` in the working directory (created on first write).
- Git milestone commits: the source mandates eleven separate commits with exact messages (`[F0-C01] …` through `[F1-C09] …`). This batch runs one OpenSpec change per proposal file and produces a single BOTL commit, so the eleven milestone messages cannot each be a separate commit here. They are listed in the BOTL commit body for traceability; this is flagged as a deviation from section 15 of the source.
