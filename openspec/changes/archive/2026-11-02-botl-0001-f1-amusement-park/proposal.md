# Proposal

## Why

The amusement-park project has no application yet. Delivery 1 needs an independently runnable Java 21 / Maven application that manages users persistently through API-like text commands, usable both interactively and from a command file, as the foundation for later deliveries (attractions, groups, tickets, passes) without implementing them prematurely.

## What Changes

The change is delivered in three incremental stages inside this single change, each keeping the project buildable and tested and preserving the previous stage's behavior:

1. **Bootstrap** — Generate a Maven project from `org.apache.maven.archetypes:maven-archetype-quickstart`, compiled with JDK 21, with a main entry-point class, the archetype's basic test structure, and an executable JAR. No domain functionality.
2. **User management** — Encapsulated user model (first name, surname(s), DNI, email, password, telephone, card number; immutable DNI); `USER CREATE`, `USER DELETE`, `USER UPDATE` commands parsed with `String.split`; Spanish DNI checksum, email (`texto@texto.xxx`), password (upper/lower/digit/symbol) and 16-digit card validation without regular expressions; uniqueness by DNI and email; semicolon-separated text-file persistence handled by a dedicated class and reloaded at startup; an `enum` of updatable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
3. **Execution and control** — Interactive mode (no arguments, standard input) and file mode (first argument is a command file) sharing one command-processing path; `HELP` and `EXIT`; blank lines ignored; invalid commands and unreadable files handled in a controlled way.

Out of scope: attractions, groups, tickets, fast passes, encryption, databases, frameworks, third-party runtime libraries, regular expressions, and design patterns introduced only for later deliveries.

## Capabilities

### New Capabilities
- `project-build`: Java 21 / Maven quickstart project structure, test execution with `mvn test`, and executable JAR packaging.
- `user-management`: User data model, validation rules, uniqueness constraints, and the `USER CREATE`, `USER DELETE` and `USER UPDATE` commands.
- `user-persistence`: Storage of users in a semicolon-separated text file (one user per line) and their reconstruction when the application starts.
- `command-execution`: Interactive and file-based command input sharing one processing path, the `HELP` and `EXIT` commands, and controlled handling of invalid commands and input files.

### Modified Capabilities
<!-- None: no existing specs. -->

## Impact

- New Maven project at the repository root (`pom.xml`, `src/main/java`, `src/test/java`).
- New runtime artifact: executable JAR under `target/`.
- A users text file is created/updated in the working directory at runtime.
- Dependencies: Java standard library only at runtime; the archetype's JUnit test dependency for tests.
