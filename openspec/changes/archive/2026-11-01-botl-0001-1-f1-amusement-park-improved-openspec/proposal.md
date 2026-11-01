# Proposal

## Why

The amusement-park project has no application yet. Delivery 1 (F1) must establish an independently runnable Java 21 / Maven application, then add persistent user management driven by API-like text commands that can be supplied either interactively or from a command file. This is the foundation later deliveries (attractions, groups, tickets, passes) will build on, so it must be delivered without anticipating them.

## What Changes

The change is implemented in three ordered stages inside this single change; each stage keeps the project buildable and its tests green, and preserves the behavior of the previous stage:

1. **Bootstrap** — Generate the project from `org.apache.maven.archetypes:maven-archetype-quickstart` on JDK 21, keep the standard layout, provide a main class, the basic JUnit test structure, and an executable JAR. No domain behavior yet.
2. **User management** — Add an encapsulated `User` model (immutable DNI), validation (Spanish DNI checksum, `texto@texto.xxx` email with a 3-character extension, password character categories, 16-digit card), DNI/email uniqueness, and the commands `USER CREATE`, `USER DELETE`, `USER UPDATE` (editable fields as an enum). Users persist to a semicolon-separated text file, one user per line, through a dedicated persistence class, and are reloaded at startup.
3. **Execution and control** — One shared command processor fed either by standard input (no arguments) or by a command file (first argument). Add `HELP` and `EXIT`; blank file lines are ignored; invalid commands, missing parameters, and unreadable files are handled without uncaught exceptions.

Explicitly out of scope: attractions, groups, tickets, fast passes, encryption, databases, frameworks, third-party libraries, regular expressions, and design patterns introduced for later phases.

## Capabilities

### New Capabilities
- `project-build`: Java 21 / Maven Quickstart project structure, test execution via `mvn test`, and executable-JAR packaging with an application entry point.
- `user-management`: User model and its responsibilities, data validation rules, identifier uniqueness, and the `USER CREATE` / `USER DELETE` / `USER UPDATE` operations.
- `user-persistence`: Text-file storage of users (semicolon-separated, one per line) by a dedicated class, and reconstruction of users at startup.
- `command-execution`: Interactive and file-based command input sharing one processing path, semicolon command parsing, `HELP`, `EXIT`, and controlled handling of invalid input and unreadable files.

### Modified Capabilities
<!-- None: no existing specs. -->

## Impact

- New Maven project (`pom.xml`, `src/main/java`, `src/test/java`) at the repository root.
- Only the Java standard library at runtime; JUnit as the test-scope dependency provided by the archetype.
- A user data file is created and maintained by the application in its working directory.
- No existing code, APIs, or specs are affected.
