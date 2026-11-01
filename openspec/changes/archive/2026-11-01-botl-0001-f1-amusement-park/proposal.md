# Proposal

## Why

The amusement-park project has no application yet. Delivery 1 (F1) needs an independently runnable Java 21 / Maven application with persistent user management and a single text-command interface usable from the terminal or from a command file, as the foundation for later amusement-park features.

## What Changes

Implemented incrementally inside this single change, each stage keeping the build green and preserving the previous stage's behaviour:

1. **Bootstrap** — Maven Quickstart (`maven-archetype-quickstart`) project compiled for JDK 21, with a main class, basic JUnit test structure, and an executable JAR. No domain functionality in this stage.
2. **User management** — an encapsulated `User` model (immutable DNI), validation of DNI (checksum letter), email (`texto@texto.xxx`, 3-character extension, no regex), password (upper/lower/digit/symbol, no regex), card (exactly 16 digits); DNI and email uniqueness; `USER CREATE`, `USER DELETE`, `USER UPDATE` commands parsed with `String.split`; an `enum` of updatable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); persistence of users in a semicolon-separated text file through a dedicated class, reloaded at startup.
3. **Execution and control** — interactive mode (stdin, no arguments) and file mode (first argument is a command-file path) sharing one command processor; blank lines ignored; `HELP` listing all commands and syntax; `EXIT` ending interactive mode and stopping file processing immediately; controlled handling of invalid commands, missing parameters, and unreadable files.

Out of scope: attractions, groups, tickets, fast passes, encryption, databases, frameworks, third-party runtime libraries, regular expressions, and design patterns introduced only for future deliveries.

## Capabilities

### New Capabilities
- `project-build`: Java 21 / Maven Quickstart project structure, test execution with `mvn test`, and executable JAR packaging.
- `user-management`: User model, input validation rules, uniqueness constraints, and the `USER CREATE` / `USER DELETE` / `USER UPDATE` commands.
- `user-persistence`: Storage of users in a semicolon-separated text file and their reconstruction at startup, owned by a dedicated persistence class.
- `command-execution`: Interactive and file-based command input sharing one processor, `HELP`, `EXIT`, blank-line handling, and controlled error handling.

### Modified Capabilities
<!-- None: there are no existing specs. -->

## Impact

- New Maven project at the repository root (`pom.xml`, `src/main/java`, `src/test/java`).
- Only the Java standard library at runtime; JUnit as the test-scope dependency provided by the Quickstart archetype structure.
- A users text file is created in the working directory at runtime.
- No existing code or specs are affected.
