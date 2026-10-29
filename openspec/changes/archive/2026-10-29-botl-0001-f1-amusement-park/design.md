# Design

## Context

The repository holds only OpenSpec scaffolding; there is no Java code yet. Motivation and scope: see proposal.md. Behavioural contracts: see `specs/`. Hard constraints from the assignment: JDK 21, Maven quickstart layout, executable JAR, Java standard library only, no regular expressions, no databases/frameworks/encryption, String/`split`-based parsing, and separate classes for user, main and persistence.

## Goals / Non-Goals

**Goals:**
- A small layered design where later deliveries (groups, tickets, attractions) can add commands without touching the input loop.
- One command-processing path shared by interactive and file modes.
- Validation in one reusable place, used by both creation and update.
- Every layer testable without spawning a JVM (inject I/O streams and the storage path).

**Non-Goals:**
- Delivery 2/3 features (system date/price, groups, tickets, attractions).
- Password hashing or phone validation (explicitly not required).
- Internationalised messages.

## Decisions

### Project generation
Generate the skeleton with `org.apache.maven.archetypes:maven-archetype-quickstart:1.5` (groupId `es.upm.amusementpark`, artifactId `amusement-park`), then set `maven.compiler.release=21`. This satisfies "created using the apache-maven-quickstart archetype" literally, and the 1.5 archetype ships JUnit 5 (test scope only). *Alternative:* hand-writing an equivalent layout — rejected because the source mandates the archetype.

### Executable JAR
Configure `maven-jar-plugin` with `<mainClass>es.upm.amusementpark.App</mainClass>` and set `<finalName>nombre-aplicacion</finalName>` so the artifact is exactly `target/nombre-aplicacion.jar`. No shading is needed because there are no runtime dependencies. *Alternative:* a meaningful name such as `amusement-park.jar` — rejected because path names are contractual (see proposal ambiguity 2).

### Package layout (`es.upm.amusementpark`)
- `App` — entry point only: builds the object graph, picks interactive vs file mode, prints a controlled error for an unreadable file. No user file I/O.
- `model.User` — encapsulated fields, private `final String dni`, getters, setters for the six modifiable attributes only.
- `model.UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
- `validation.UserValidator` — static pure methods `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, all char/`split`-based.
- `persistence.UserRepository` — loads/saves `List<User>` to a `Path` (default `users.txt`), uses `split(";", -1)` and `Files` APIs.
- `service.UserService` — holds users in memory (`LinkedHashMap` by email to preserve insertion order), enforces validation and uniqueness, calls the repository after each successful mutation. Throws a single checked/unchecked `UserOperationException` with a human-readable reason on rejection.
- `command.CommandProcessor` — `boolean process(String line)` returns `false` when `EXIT` was processed; parses with `split(";", -1)`, dispatches on the first element, writes results to an injected `PrintStream`.
- `command.CommandRunner` — `run(BufferedReader)` loops: read line, trim, skip blank, call `process`, stop on `false` or EOF. Used by both modes (stdin wrapped in a reader, or `Files.newBufferedReader(path)`), which guarantees identical behaviour.

*Alternative:* a Command interface per operation (Command pattern) — deferred; patterns are not required in Delivery 1 and a `switch` keeps it small.

### `split` usage and the "no regex" rule
`String.split(String)` technically compiles a pattern, but for single-character non-metacharacter separators (`;`, `@`) Java uses a fast path with no regex engine, and the assignment explicitly mandates `split`. For `.` (a metacharacter) the email validator uses `lastIndexOf('.')`/`substring` instead of `split("\\.")` to avoid any regex syntax.

### Messages
Success: `USER CREATED`, `USER DELETED`, `USER UPDATED`. Failure: `ERROR: <reason>`. Help prints one syntax line per command. Malformed persistence line: `WARNING: ...` on stderr.

### Field-count rules
`USER CREATE` requires exactly 8 elements (command + 7), `USER DELETE` 3, `USER UPDATE` 5; `HELP`/`EXIT` exactly 1. Using `split(";", -1)` means trailing empty fields count, so `USER DELETE;a@b.com;` is a 3-element command with an empty password (rejected by credential check).

## Risks / Trade-offs

- [`;` inside a value (e.g. a password containing `;`) cannot be represented] → inherent to the mandated format; such a value would produce a wrong field count and be rejected.
- [`users.txt` is relative to the working directory, so running from different folders sees different data] → documented; matches the unspecified-location ambiguity.
- [Plain-text passwords on disk] → explicitly required by the assignment (no encryption in Delivery 1).
- [Single commit instead of eleven milestone commits] → recorded in proposal ambiguity 1 and in the commit body.

## Migration Plan

Not applicable (greenfield). Rollback = revert the commit.
