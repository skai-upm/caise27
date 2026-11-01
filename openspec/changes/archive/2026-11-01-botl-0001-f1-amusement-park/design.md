# Design

## Context

The repository contains only OpenSpec scaffolding; there is no Java code yet. See proposal.md for motivation and specs/ for the required behaviour. The build machine runs a newer JDK than 21, so the compiler target must be pinned to release 21 rather than relying on the installed JDK version.

## Goals / Non-Goals

**Goals:**
- A minimal, readable class structure that satisfies the responsibility separation required by the specs.
- One command processor used by both input modes, so behaviour cannot drift between them.
- Failed operations never mutate in-memory or persisted state.

**Non-Goals:**
- Interfaces, layers, or patterns that only serve future deliveries.
- Configurable storage locations exposed to end users.

## Decisions

### Project layout and build
- Generated from `maven-archetype-quickstart`; `groupId` `es.upm.amusementpark`, `artifactId` `amusement-park`, base package `es.upm.amusementpark`.
- `maven.compiler.release=21` so the bytecode targets Java 21 regardless of the build JDK.
- Executable JAR via `maven-jar-plugin` manifest `mainClass=es.upm.amusementpark.App`. Alternative (shade/assembly plugin) rejected: there are no runtime dependencies to bundle.
- JUnit 5 (test scope only), as provided by the archetype.

### Classes and responsibilities
| Class | Responsibility |
|-------|----------------|
| `App` | Entry point: chooses interactive vs file mode from `args`, opens the input, reports file errors, delegates every line to `CommandProcessor`. Contains no file persistence logic. |
| `CommandProcessor` | Parses one command line with `String.split(";", -1)`, dispatches to `HELP`, `EXIT`, or user operations, returns the output text and whether execution must stop. Also provides the shared read loop over a `BufferedReader` used by both modes. |
| `User` | Encapsulated state; `dni` is `final` with no setter. |
| `UserField` | `enum` of updatable fields: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. |
| `UserValidator` | Static validation helpers (DNI checksum, email, password, card, non-empty) using only `String`/`Character` operations. |
| `UserManager` | In-memory user list, uniqueness checks, create/delete/update, and calls to the repository after each successful change. |
| `UserRepository` | Reads/writes the semicolon-separated users file; receives and returns `User` objects. |

Alternative considered: putting validation inside `User` setters. Rejected because validation messages and uniqueness checks need the manager's context, and keeping `User` a plain encapsulated holder keeps responsibilities simple.

### Command parsing
- The line is trimmed; the first `;`-separated part is the operation keyword (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), compared case-sensitively after trimming.
- `split(";", -1)` keeps trailing empty parts so missing/empty parameters are detected. Parameter counts must match exactly (8, 3, 5 parts including the keyword); otherwise an error is reported.
- Each parameter is trimmed; empty parameters are rejected.
- Output convention: successful operations print a line starting with `OK:`; failures print a line starting with `ERROR:`. This makes outputs comparable between modes and testable.

### Validation details (resolving source ambiguities)
- **DNI**: exactly 9 characters, 8 ASCII digits plus an uppercase control letter equal to `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)`. Lowercase letters are rejected (assumption: the source examples are uppercase).
- **Email**: exactly one `@`; local part nonempty; the part after `@` must contain a `.`; the extension is the text after the *last* `.` and must be exactly 3 characters; the domain before the last `.` must be nonempty. Whitespace anywhere in the email is rejected because it cannot be part of `texto`.
- **Password**: symbol = any character that is not a letter, digit, or whitespace (`Character` methods).
- **Card**: length 16 and every character `Character.isDigit` restricted to ASCII `'0'..'9'`.
- **Name / surname / telephone**: must be nonempty; telephone stored verbatim.
- **Update to own current email**: allowed (not a conflict with *another* user).

### Atomic updates and persistence
- `UserManager` performs the change on the in-memory list and then calls `UserRepository.save(users)`. If saving throws `IOException`, the in-memory change is reverted and an `ERROR:` is reported, so memory and file stay consistent.
- Update applies to a copy of the user's values: validation happens first, then mutation, so rejected updates never mutate the user.
- Users file: `users.txt` in the working directory by default; `UserManager`/`UserRepository` accept a `Path` so tests use temporary directories.
- Loading: missing file → empty list; lines that are blank or do not have exactly 7 fields are skipped with a warning on standard error rather than aborting startup.
- Values containing `;` cannot reach storage because `;` is the command separator, so the file format needs no escaping.

### Input modes
- No args → `BufferedReader` over `System.in`; ≥1 arg → `Files.newBufferedReader(Path.of(args[0]))`. Both are passed to `CommandProcessor.run(reader, out)`.
- File open/read failures (`IOException`, `InvalidPathException`) are caught in `App` and reported as `ERROR: cannot read command file ...`; the process exits with status 1 without a stack trace.
- Interactive mode prints no prompt so that interactive and file output are identical.

## Risks / Trade-offs

- [Passwords stored in plain text] → Explicitly required by the source (encryption is out of scope for F1); noted for future deliveries.
- [Strict uppercase keywords/field names may surprise users] → `HELP` documents the exact syntax; behaviour is consistent across modes.
- [Whole-file rewrite on every change] → Acceptable for the expected small data volume; simplest correct approach without a database.

## Migration Plan

Greenfield: no migration. Rollback is reverting the commit.
