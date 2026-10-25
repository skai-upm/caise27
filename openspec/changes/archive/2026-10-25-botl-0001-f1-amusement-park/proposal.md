# Proposal

## Why

The Amusement Park project has no code yet. Delivery 1 (F1) asks for a Maven/Java 21 base project generated from `maven-archetype-quickstart`, a first functional epic for user management driven by text commands with persistent storage, and the ability to run those commands either interactively from the terminal or from an input file, with `HELP` and `EXIT` general commands.

## What Changes

- Generate the base project with `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout and test structure, targeting Java 21, and configure the build so `mvn package` produces an executable JAR (`target/parque.jar`) whose entry point is the main class.
- Add a `User` model (first name, surname(s), DNI, email, password, telephone, card number) with encapsulated attributes and an immutable DNI.
- Add text-command processing for `USER CREATE`, `USER DELETE` and `USER UPDATE`, interpreted with `String.split` only. Invalid commands and wrong parameter counts are reported without terminating the program.
- Validate DNI (8 digits + check letter by the Spanish algorithm), email (`texto@texto.xxx` checked with String operations, no regex), password (uppercase, lowercase, digit, symbol, no regex) and card number (exactly 16 digits). Reject duplicate DNI or email.
- Persist users in a semicolon-separated text file, one user per line, via a class separate from both `User` and the main class; reload users at startup.
- Editable fields for `USER UPDATE` represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); DNI is not editable. Authentication by current email + password is required for delete and update.
- Interactive mode (no arguments, reads standard input until `EXIT` or end of input) and file mode (first argument is a command file read line by line), sharing a single command-processing path. Blank lines are ignored; `EXIT` stops processing immediately; missing/unreadable files are reported in a controlled way.
- `HELP` lists the available commands and their syntax; `EXIT` ends the application in a controlled manner.
- Explicitly out of scope: attractions, groups, tickets, fast passes, encryption, regular expressions, databases, frameworks and external libraries.

The source proposal describes the work as sequential steps ("project setup only, no domain functionality yet", then the user epic, then the execution modes). This change delivers all steps; the "no domain functionality" constraint applies only to the setup step and is superseded by the subsequent epics in the same proposal.

## Capabilities

### New Capabilities
- `project-build`: Maven/Java 21 project structure from the quickstart archetype, test execution with `mvn test`, and executable JAR packaging.
- `user-management`: User data model, validation rules, uniqueness, create/delete/update operations, and file persistence with reload at startup.
- `command-interface`: Text command parsing, interactive and file execution modes sharing one processor, and the general `HELP` and `EXIT` commands.

### Modified Capabilities
<!-- None: the project has no existing specs. -->

## Impact

- New Maven project files at the repository root: `pom.xml`, `src/main/java/...`, `src/test/java/...`.
- New runtime data file (`data/users.txt`, created on first write, relative to the working directory).
- No external runtime dependencies; JUnit (from the archetype) is the only test dependency.
