# Proposal

## Why

The Amusement Park project currently has no code: the repository only holds OpenSpec scaffolding. Delivery 1 (F1) asks for the Maven/JDK 21 base project generated from `maven-archetype-quickstart`, a first functional epic for basic user management driven by text commands, and the ability to run those commands interactively or from an input file with `HELP` and `EXIT` support. This change lays that foundation so later deliveries (groups, tickets, attractions) can build on a working, tested application.

## What Changes

- Generate a Maven project with `maven-archetype-quickstart`, keeping its standard layout (`src/main/java`, `src/test/java`, `pom.xml`), compiling for Java 21, with a basic test structure and an executable JAR whose manifest names the main class.
- Add a terminal application that receives API-like text commands, splitting them with `String.split` (no regular expressions, no external libraries).
- Add `USER CREATE`, `USER DELETE` and `USER UPDATE` commands with full validation:
  - Spanish DNI validated with the official check-letter algorithm (format alone is not enough).
  - Email validated with String operations (`text@text.xxx`, three-character extension), no regex.
  - Password requiring an uppercase letter, a lowercase letter, a digit and a symbol, no regex.
  - Card number of exactly 16 consecutive digits.
  - Telephone number stored as given (no processing).
  - Uniqueness of DNI and email across users.
- Store users in a semicolon-separated text file (one user per line) and reload them when the application starts again.
- `USER DELETE` requires email and matching password; `USER UPDATE` requires current email and password plus a field from an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) and revalidates the new value. The DNI is immutable.
- Add execution modes: without arguments the application reads commands from standard input; with one argument it reads commands line by line from that file. Both modes share one command processor.
- Add `HELP` (lists available commands and their syntax) and `EXIT` (controlled termination; in file mode nothing after `EXIT` runs).
- Invalid commands, missing parameters, blank lines and unreadable input files are handled without unhandled exceptions.

Out of scope: attractions, groups, tickets, fast passes, encryption, databases, frameworks, external libraries and design patterns aimed at future deliveries.

## Capabilities

### New Capabilities

- `project-build`: Maven/JDK 21 project structure, test execution and executable JAR packaging.
- `command-processing`: Text command interpretation, error handling for invalid commands, and the general `HELP` and `EXIT` commands.
- `execution-modes`: Running the application interactively from standard input or from a command file given as the first argument, sharing the same processing.
- `user-management`: Creating, deleting and updating users, including field validation, DNI immutability and DNI/email uniqueness.
- `user-persistence`: Storing users in a semicolon-separated text file and restoring them on startup.

### Modified Capabilities

(none)

## Impact

- New files: `pom.xml`, `src/main/java/...`, `src/test/java/...` generated from the quickstart archetype and extended with the user-management classes.
- Runtime data file for users (default `data/users.txt`, relative to the working directory) — ignored by Git.
- No external runtime dependencies; JUnit (from the archetype) is the only test dependency.
