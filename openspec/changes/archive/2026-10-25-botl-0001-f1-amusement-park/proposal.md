# Proposal

## Why

The amusement park project starts from an empty repository. It needs a Maven/Java 21 base generated from `maven-archetype-quickstart`, and then a first functional epic: basic user management driven by API-like text commands, with file persistence, plus the ability to run those commands interactively or from an input file with `HELP` and `EXIT` support (Delivery 1).

## What Changes

- Create a Maven project from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout (`src/main/java`, `src/test/java`, `pom.xml`), compiling for Java 21, with an executable main class and a JAR whose manifest names that main class. `mvn test` must succeed.
- Add a `User` model with encapsulated attributes (first name, surname(s), DNI, email, password, telephone, card number); the DNI is immutable after creation.
- Add validation using only the Java standard library and no regular expressions: Spanish DNI (8 digits + check letter by the standard modulo-23 algorithm), email (`text@text.xxx` with a three-character extension, checked via `String` operations and `split`), password (uppercase, lowercase, digit, symbol), card number (exactly 16 digits).
- Add commands `USER CREATE`, `USER DELETE`, `USER UPDATE`, parsed with `String.split` on `;`. Uniqueness of DNI and email is enforced. Editable fields for update are represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`); the DNI cannot be updated.
- Add a separate persistence class storing one user per line in a `;`-separated text file and reloading users at startup.
- Add execution modes: interactive (no arguments, reads standard input) and file mode (first argument is a commands file read line by line), sharing one command-processing mechanism. Blank lines are ignored.
- Add `HELP` (lists available commands and syntax) and `EXIT` (controlled termination; nothing after `EXIT` runs in file mode).
- Invalid commands, insufficient parameters, and unreadable/nonexistent input files are reported without unhandled exceptions.
- Out of scope: attractions, groups, tickets, fast passes, encryption, regular expressions, databases, frameworks, external libraries, and speculative design patterns.

## Capabilities

### New Capabilities
- `project-build`: Maven/Java 21 project structure, build, tests and executable JAR.
- `user-management`: User model, field validation, uniqueness, and the `USER CREATE` / `USER DELETE` / `USER UPDATE` operations.
- `user-persistence`: Storing users in a semicolon-separated text file and reloading them at startup.
- `command-interface`: Command parsing with `String.split`, interactive and file execution modes, `HELP`, `EXIT`, and controlled error handling.

### Modified Capabilities
<!-- None: the project has no existing specs. -->

## Impact

- New files: `pom.xml`, `src/main/java/...` (application main class, command processor, user model, validators, editable-field enum, user repository/persistence), `src/test/java/...` (JUnit tests).
- Dependencies: only JUnit for tests (supplied by the archetype); no runtime dependencies.
- Runtime data: a users text file (default `users.txt` in the working directory).
