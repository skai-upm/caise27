# Proposal

## Why

The Amusement Park project has no code yet: the repository only contains OpenSpec scaffolding. Delivery 1 (F1) asks for the foundation of the application — a Maven/JDK 21 project generated from `maven-archetype-quickstart` — plus the first functional epic (user management driven by text commands with file persistence) and the execution modes (interactive terminal or command file, with `HELP` and `EXIT`). All later deliveries (date, groups, tickets, attractions) build on this base, so it must exist first.

## What Changes

- Generate a Maven project from `org.apache.maven.archetypes:maven-archetype-quickstart`, configured for Java 21, keeping the archetype's standard layout (`src/main/java`, `src/test/java`, JUnit test structure) and producing an executable JAR whose manifest points to the main class.
- Add a terminal application that receives API-like text commands, splitting each command with `String.split` (no regular expressions, no external libraries).
- Add the user model (first name, surname(s), DNI, email, password, telephone, card number) with encapsulated attributes and an immutable DNI.
- Add the commands `USER CREATE`, `USER DELETE` and `USER UPDATE`, including full data validation (Spanish DNI check letter, email format `texto@texto.xxx`, password complexity, 16-digit card) and uniqueness of DNI and email.
- Add an editable-field enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) for `USER UPDATE`; DNI is not editable.
- Persist users in a semicolon-separated text file (one user per line) through a class independent of both the user class and the main class; reload users at startup.
- Support two execution modes sharing one command processor: interactive (no arguments, reads standard input) and file mode (first argument is a command file, processed line by line, blank lines ignored).
- Add `HELP` (lists available commands and syntax) and `EXIT` (controlled termination; in file mode no later command runs).
- Handle invalid commands, missing parameters and unreadable input files without terminating with an unhandled exception.

## Capabilities

### New Capabilities
- `project-build`: Maven/JDK 21 project structure derived from the quickstart archetype, test structure, and executable JAR packaging.
- `command-processing`: Text-command interpretation with `String.split`, interactive and file execution modes, shared processing, `HELP`, `EXIT`, and error tolerance for malformed commands and input files.
- `user-management`: User model, `USER CREATE` / `USER DELETE` / `USER UPDATE` commands, data validation rules, and DNI/email uniqueness.
- `user-persistence`: Storing users in a semicolon-separated text file and restoring them when the application starts.

### Modified Capabilities
- None (no existing specs).

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**` (Maven quickstart layout).
- New runtime data file for stored users (created at runtime, not versioned).
- Dependencies: only the Java standard library at runtime; JUnit (from the archetype) for tests.
- Out of scope for this change: attractions, groups, tickets, fast passes, encryption, databases, frameworks, regular expressions, and any feature from later deliveries.
