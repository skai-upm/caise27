# Proposal

## Why

The amusement-park project has no application yet. Delivery 1 (F1) needs an independently runnable Java 21 / Maven application that manages users persistently through a text-command interface usable from the terminal or from a command file. This is the foundation for later amusement-park features, which must not be introduced prematurely.

## What Changes

Implemented in three incremental stages inside this single change, each keeping the project buildable and tested and preserving the previous stage:

1. **Bootstrap** — Maven Quickstart project (standard `maven-archetype-quickstart` layout) compiled for JDK 21, with a main entry-point class, basic JUnit test structure, and an executable JAR. No domain functionality.
2. **User management** — Encapsulated `User` model (first name, surname(s), immutable DNI, email, password, telephone, card); validation of Spanish DNI (checksum letter), email (`texto@texto.xxx`, 3-char extension, no regex), password (upper/lower/digit/symbol, no regex), card (exactly 16 digits); DNI and email uniqueness; `USER CREATE`, `USER DELETE`, `USER UPDATE` commands parsed with `String.split`; an `enum` of updatable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); persistence to a semicolon-separated text file in a class independent of `User` and the main class, reloaded at startup.
3. **Execution and control** — Interactive mode (stdin, no arguments) and file mode (first argument is a command file) sharing one command processor; `HELP` and `EXIT`; blank lines ignored; invalid commands and unreadable files handled without uncontrolled termination.

Out of scope: attractions, groups, tickets, passes, encryption, databases, frameworks, third-party runtime libraries, regular expressions, and design patterns introduced only for later deliveries.

## Capabilities

### New Capabilities
- `project-build`: Java 21 / Maven Quickstart project structure, test execution with `mvn test`, and executable JAR packaging.
- `user-management`: User model, input validation (DNI, email, password, card), uniqueness, and the `USER CREATE` / `USER DELETE` / `USER UPDATE` commands.
- `user-persistence`: Storage of users in a semicolon-separated text file and their reconstruction at startup, in a dedicated persistence class.
- `command-execution`: Interactive and file input modes sharing one command processor, `HELP`, `EXIT`, and controlled handling of invalid commands and unreadable files.

### Modified Capabilities
<!-- None: no main specs exist yet. -->

## Impact

- New files: `pom.xml`, `src/main/java/...` (application, user model, validation, persistence, command processing), `src/test/java/...` (JUnit tests).
- Build dependencies: Maven plugins for compilation, tests (JUnit as test-scope dependency from the Quickstart archetype), and executable-JAR manifest. No runtime third-party libraries.
- Runtime artifact: a text file holding user records in the working directory.
