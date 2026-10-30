# Proposal

## Why

The repository contains no application yet. Amusement Park Delivery 1 (milestones F0-C01 through F1-C09, source: `.botl/proposals/opus_4_setup_single_gpt/0001-1.F1-Amusement-Park/1.F1 - Amusement Park.md`) requires a Java 21 Maven project that builds into an executable JAR and manages park users (create, delete, update) through a `;`-separated text command interface, with file-based persistence and input from either standard input or a command file. This change delivers that whole first delivery as one coherent OpenSpec change.

## What Changes

- **F0-C01** Create a standard Maven project (apache-maven-quickstart layout) configured for JDK 21 and compilable with `mvn package`.
- **F0-C02** Configure the build so `mvn package` produces an executable JAR with a declared entry-point class, runnable with `java -jar target/<jar>.jar` (interactive) and `java -jar target/<jar>.jar input.txt` (file mode).
- **F1-C01** Add an encapsulated `User` domain class (first name, surname(s), DNI, email, password, phone, card) whose DNI cannot change after creation, plus separate classes for the entry point and for storing/retrieving users.
- **F1-C02** Add reusable user-data validation for DNI (8 digits + check letter, Spanish algorithm), email (`text@text.xxx`, String operations/split, no regex), password (uppercase, lowercase, digit, symbol; no regex) and card (exactly 16 digits). Phone is stored without validation.
- **F1-C03** Add the `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` command, enforcing validation and uniqueness of DNI and email.
- **F1-C04** Persist users to a text file (one user per line, `;`-separated attributes in the order shown in the source) via a dedicated persistence class; load them at start-up.
- **F1-C05** Add `USER DELETE;Email;Password` (locate by email, verify existence, check password, delete, persist).
- **F1-C06** Add `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; DNI stays immutable; creation validation and email uniqueness still apply; changes persist.
- **F1-C07** Accept a command file path as the first argument; process its lines in order, ignore blank lines, share one command-processing implementation with interactive mode, and handle a missing/unreadable file in a controlled way.
- **F1-C08** Add `HELP` (lists available commands and syntax) and `EXIT` (controlled termination; in file mode no later line is processed).
- **F1-C09** Integrate everything: unknown or malformed commands never terminate the application unexpectedly; build from scratch with `mvn package`; JAR runs without an IDE in both modes.

### Flagged ambiguities and decisions (not new business rules)

1. **Per-milestone Git commits.** The source mandates eleven commits with exact messages (`[F0-C01] Initialize Maven project with JDK 21` … `[F1-C09] Complete delivery 1 integration`). This change is executed by the BOTL batch harness, which produces exactly one commit per proposal file with its own subject format. The mandatory milestone commit history therefore cannot be produced by this change and is **out of scope**; it is recorded here as a known deviation.
2. **JAR name.** The source uses the placeholder `target/nombre-aplicacion.jar` ("application name"). The JAR will be named `target/amusement-park.jar`.
3. **Persistence file location.** Not specified; the users file will be `users.txt` in the current working directory.
4. **Line-wrapped examples.** Source examples such as `...;Clave1` / `!;612345678;...` are line-wrapping artefacts of the original document; they are interpreted as a single line with password `Clave1!`.
5. **"Symbol"** for password validation is interpreted as any character that is neither a letter, a digit, nor whitespace.
6. **DNI letter case.** The check letter must be uppercase, as in the source example `12345678Z`.
7. **Output messages.** The source states exact messages may vary; success messages will be `USER CREATED`, `USER DELETED`, `USER UPDATED`, and failures are reported as a single line starting with `ERROR:`.
8. **Malformed input.** A command with the wrong number of `;`-separated parts, or with a blank required value, is rejected with an error and processing continues.
9. **Testing library.** The quickstart archetype declares JUnit as a test-scoped dependency. It is used only for tests and is not packaged into the JAR, so the runtime still uses only the Java standard library.

## Capabilities

### New Capabilities
- `project-build`: Maven/JDK 21 project structure, `mvn package` build and executable JAR entry point.
- `user-validation`: Validation rules for DNI, email, password and card number.
- `user-management`: User domain model and the USER CREATE / USER DELETE / USER UPDATE operations, including uniqueness and DNI immutability.
- `user-persistence`: Text-file storage of users and loading at start-up.
- `command-interface`: `;`-separated command parsing, interactive and file input modes, HELP and EXIT, and controlled handling of invalid input.

### Modified Capabilities
<!-- None: no specs exist yet. -->

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...` (standard Maven layout).
- Runtime: Java standard library only; no databases, frameworks, regular expressions or encryption.
- Build: Maven 3 with `maven.compiler.release` 21; test-scoped JUnit only.
- Runtime data file: `users.txt` (created in the working directory when users are stored; ignored by Git).
