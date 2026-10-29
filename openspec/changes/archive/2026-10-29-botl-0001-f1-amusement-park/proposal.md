# Proposal

## Why

The Amusement Park assignment (Delivery 1) requires a Java 21 / Maven command-line application that manages park users: create, validate, persist, delete and update them through `;`-separated text commands, both interactively and from a command file. The repository currently contains no code, so this change delivers the complete Delivery 1 scope (milestones F0-C01, F0-C02 and F1-C01 … F1-C09) as described in the source proposal `1.F1 - Amusement Park.md`.

## What Changes

- **F0-C01** — Create a standard Maven project (apache-maven-quickstart layout) configured for JDK 21 and compilable with `mvn package`.
- **F0-C02** — Configure the build so `mvn package` produces an executable JAR (`target/nombre-aplicacion.jar`) with a declared entry-point class, runnable with `java -jar` with no argument (interactive) or with a command-file argument.
- **F1-C01** — Introduce an encapsulated `User` domain class (first name, surname(s), DNI, email, password, phone, card) whose DNI cannot change after creation; keep separate classes for the user, the application entry point and user storage.
- **F1-C02** — Add reusable validation for DNI (8 digits + correct control letter), email (`text@text.xxx`, String operations/`split`, no regex), password (upper, lower, digit, symbol; no regex/libraries) and card (exactly 16 digits). Phone is stored without validation.
- **F1-C03** — Implement `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, rejecting invalid data and duplicate DNI or email.
- **F1-C04** — Persist users to a text file (one user per line, attributes separated by `;`) through a dedicated persistence class; load them at start-up. No databases, frameworks or external libraries.
- **F1-C05** — Implement `USER DELETE;Email;Password` (locate by email, verify existence, check password, delete, persist).
- **F1-C06** — Implement `USER UPDATE;Email;Password;Field;NewValue` with an enum of modifiable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`), DNI immutable, same validation/uniqueness rules as creation, and persisted changes.
- **F1-C07** — Support file mode (`java -jar … input.txt`): commands processed in order, blank lines ignored, same processing path as interactive mode, controlled handling of a missing/unreadable file.
- **F1-C08** — Add `HELP` (lists available commands and syntax) and `EXIT` (controlled termination; nothing after `EXIT` runs, also in file mode).
- **F1-C09** — Integrate everything: malformed or unknown input never crashes the application; the project builds from scratch with `mvn package` and the JAR runs outside an IDE in both modes. Technical restrictions: Java standard library only; no databases, frameworks, external libraries, regular expressions or encryption.

### Flagged ambiguities (recorded, not silently resolved)

1. **Mandatory per-milestone commits vs. batch execution.** The source requires eleven separate commits with exact messages (`[F0-C01] …` through `[F1-C09] …`). This change is executed by a batch orchestrator that mandates exactly one commit per proposal file with its own subject format. The eleven milestone messages will be listed in that commit's body for traceability, but the required separate milestone commits cannot be produced within this lifecycle and must be reconciled by the course owner.
2. **JAR name.** The source writes `target/nombre-aplicacion.jar` ("application-name"), which looks like a placeholder. Because path names are contractual, the build will produce exactly `target/nombre-aplicacion.jar`.
3. **Line-wrapped examples.** Examples such as `…;Clave1` / `!;612345678;…` are split across two lines in the source (a formatting artifact). They are interpreted as single commands with password `Clave1!` (and `Otra2#`), consistent with the persistence and update examples.
4. **Persistence file location/name** is not specified. A file named `users.txt` in the current working directory will be used.
5. **"Symbol"** in password rules is not defined. Any character that is not a letter, digit or whitespace counts as a symbol.
6. **Email domain** — the source requires content before `@`, content after `@`, a domain and an extension of exactly three characters. The domain part is split at its last `.`; the text before it must be non-empty and the text after it must have exactly three characters. Exactly one `@` is required. No further character restrictions are invented.
7. **First name, surname(s) and phone** have no stated format rules; they are stored as given (no content validation invented). A command with the wrong number of `;`-separated fields is rejected as malformed.
8. **Command keywords and field names** are treated as case-sensitive, exactly as written in the source (`USER CREATE`, `NAME`, …).
9. **Output messages** — the source says messages may vary; success messages follow the example (`USER CREATED`, `USER UPDATED`, `USER DELETED`); errors are reported as a line starting with `ERROR:`.
10. **Test framework** — the quickstart archetype brings a test-scoped JUnit dependency. It is used only for tests and is not part of the runtime JAR, so the "standard library only" restriction is interpreted as applying to application code.

## Capabilities

### New Capabilities

- `project-build`: Maven/JDK 21 project structure and executable JAR packaging (F0-C01, F0-C02, build-related F1-C09 restrictions).
- `user-model`: The encapsulated user domain object and its immutable DNI (F1-C01).
- `user-validation`: Validation rules for DNI, email, password and card number, reusable by all user operations (F1-C02).
- `user-management`: `USER CREATE`, `USER DELETE`, `USER UPDATE` operations including uniqueness rules and the modifiable-field enum (F1-C03, F1-C05, F1-C06).
- `user-persistence`: Text-file storage and start-up loading of users (F1-C04).
- `command-interface`: Command parsing, interactive and file input modes, `HELP`, `EXIT`, and controlled handling of invalid input (F1-C07, F1-C08, F1-C09).

### Modified Capabilities

(none — no existing specs)

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Runtime: JDK 21, Maven; no runtime dependencies beyond the Java standard library.
- Creates/rewrites `users.txt` in the working directory at runtime (ignored by Git).
