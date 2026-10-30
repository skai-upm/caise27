# Design

## Context

The repository contains only OpenSpec scaffolding; there is no Java code. See proposal.md for motivation. Requirements live in `specs/project-build`, `specs/user-management`, `specs/user-persistence` and `specs/command-execution`. Constraints: Java 21, Maven Quickstart layout, Java standard library only, no regular expressions, commands parsed with `String.split`.

## Goals / Non-Goals

**Goals:**
- Minimal, readable class set that satisfies the mandated responsibility separation (user model / main + terminal interaction / file persistence).
- One command processor shared by stdin and file modes.
- Every failed operation leaves in-memory and persisted state untouched.
- Testable without spawning processes: command processor writes to an injectable `PrintStream`, repository takes a file path.

**Non-Goals:**
- Interfaces, design patterns, or layers added for future deliveries.
- Encryption/hashing of passwords or telephone numbers.
- Escaping of `;` inside values (the command syntax itself cannot carry it).

## Decisions

1. **Coordinates and layout.** `groupId` `es.upm.amusementpark`, `artifactId` `amusement-park`, package `es.upm.amusementpark`. Standard Quickstart directories. `maven.compiler.release=21`, so the build targets Java 21 even on newer JDKs. JUnit Jupiter (test scope) as in current Quickstart archetype versions.
2. **Executable JAR.** `maven-jar-plugin` with `<mainClass>es.upm.amusementpark.App</mainClass>` in the manifest. No shading is needed since there are no runtime dependencies. Alternative (assembly/shade plugin) rejected as unnecessary.
3. **Classes.**
   - `App` — `main`: selects mode by `args.length`, opens stdin or the file as a `BufferedReader`, feeds lines to `CommandProcessor`, catches `IOException` for controlled file errors. No file-storage logic.
   - `User` — encapsulated fields, `final` DNI, getters, setters for editable fields only.
   - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed with `valueOf` inside try/catch (no `DNI` constant, so `DNI` is rejected naturally).
   - `UserValidator` — static checks: `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNotBlank`; character loops and `String` methods only.
   - `UserRepository` — loads/saves `List<User>` from/to a `Path`, `;`-separated, one per line (UTF-8). Only class touching the users file.
   - `UserService` — in-memory list plus repository; `create`, `delete`, `update` return a result message; validates and checks uniqueness before mutating, then saves the whole list.
   - `CommandProcessor` — `process(String line)` returns `boolean` (false = EXIT); splits on `;`, dispatches `USER CREATE|DELETE|UPDATE`, `HELP`, `EXIT`; prints `OK: ...` / `ERROR: ...` to its `PrintStream`. Catches `RuntimeException` per command as a final safety net so a bad command never terminates the app.
   Alternative of a command-object pattern rejected: explicitly disallowed premature pattern.
4. **Parsing.** `line.trim()`, `split(";", -1)` so empty trailing fields are counted and detected. Operation token is `parts[0].trim()` compared case-sensitively (`USER CREATE`, etc.). Exact parameter counts: CREATE 8 parts, DELETE 3, UPDATE 5; any other count → error.
5. **Update atomicity.** Validate new value (and email uniqueness excluding the same user) before applying the setter; then persist. Password check precedes everything.
6. **Persistence timing.** Save the full list after each successful mutation (rewrite whole file). Simple and adequate for small data. File path defaults to `users.txt` in the working directory.
7. **Loading malformed lines.** Lines that do not have exactly 7 fields are skipped when loading (with a warning) rather than aborting startup.

## Risks / Trade-offs

- [Passwords stored in plain text] → Explicitly required by the source (encryption out of scope); documented.
- [Whole-file rewrite on each change] → Acceptable at this scale; avoids partial-line edits.
- [Running from different working directories uses different `users.txt`] → Documented default; tests use temp directories.

## Migration Plan

Greenfield; no migration. Rollback = revert the change.

## Open Questions

- Assumption recorded: first name, surname(s) and telephone must be non-empty (the source requires "validate all data" but gives no further rule for these). Telephone is otherwise stored as given.
- Assumption recorded: "symbol" means any character that is not a letter, digit or whitespace.
- Assumption recorded: an email is valid when the substring after `@` contains a period, the part before the last period is non-empty, and the part after it has exactly 3 characters; no second `@` is allowed.
