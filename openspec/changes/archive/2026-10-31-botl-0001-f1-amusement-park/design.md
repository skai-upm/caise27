# Design

## Context

The repository contains only OpenSpec scaffolding; there is no Java code yet. See proposal.md for motivation and scope. Constraints from the request that shape the design: Java 21, Maven Quickstart layout, Java standard library only, no regular expressions, commands split with `String.split`, text-file persistence, and no patterns or layers added only for future deliveries.

## Goals / Non-Goals

**Goals:**
- Small, readable set of classes with the three mandated responsibilities kept separate (user state, application entry/terminal interaction, file persistence).
- One command-processing path shared by stdin and file input.
- Every rejected operation leaves in-memory and on-disk state unchanged.
- Fully testable without spawning processes: input/output streams and the data-file path are injectable.

**Non-Goals:**
- Interfaces, dependency-injection frameworks, command-pattern hierarchies, or any abstraction aimed at later deliveries.
- Securing stored passwords or phone numbers (encryption is explicitly excluded).
- Concurrency or multi-process access to the user file.

## Decisions

### D1. Project coordinates and layout
Generate (or reproduce exactly) the `maven-archetype-quickstart` layout at the repository root with `groupId=es.upm.amusementpark`, `artifactId=amusement-park`, package `es.upm.amusementpark`. `maven.compiler.release=21`. JUnit Jupiter is the only dependency, in `test` scope.
*Alternative:* sub-directory project — rejected; the repository is dedicated to this application.

### D2. Executable JAR
Configure `maven-jar-plugin` with `archive.manifest.mainClass=es.upm.amusementpark.App`. No shading is needed because there are no runtime dependencies.
*Alternative:* `maven-assembly-plugin`/shade — unnecessary without runtime dependencies.

### D3. Classes and responsibilities
| Class | Responsibility |
|---|---|
| `App` | `main`: selects interactive vs. file mode from `args`, opens the input source, handles file-open/read errors, delegates each line to `CommandProcessor`. Contains no file persistence code for users. |
| `CommandProcessor` | Shared interpreter: trims the line, ignores blanks, splits with `split(";", -1)`, dispatches `USER CREATE/DELETE/UPDATE`, `HELP`, `EXIT`; prints result messages; returns whether processing should continue. |
| `UserManager` | In-memory user collection plus business rules: creation, authentication, deletion, update, uniqueness; calls `UserRepository.save` after each successful change. |
| `UserValidator` | Static validation helpers for DNI, email, password, card and non-blank text (no regex). |
| `User` | Encapsulated state; `dni` is `final` with no setter. |
| `UserField` | Enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`. |
| `UserRepository` | Reads/writes the semicolon-separated user file; receives/returns `User` objects. |

`UserManager` and `UserValidator` are not required by the request, but keep `App` free of business logic and keep `CommandProcessor` focused on parsing; they are plain classes, not a layered architecture.

### D4. Command parsing
A line is trimmed; if empty it is skipped (no output). It is split with `split(";", -1)` so that trailing empty fields are counted. The first token (trimmed) is the operation and must match exactly (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`; case-sensitive, as documented by `HELP`). Each operation requires an exact parameter count (7, 2, 4, 0, 0); a different count yields an `ERROR` message. Parameter values are used as supplied (not trimmed) so that stored values equal the input. Because `;` is the separator, no value can contain `;`, which keeps the file format unambiguous.

### D5. Output conventions
Successful operations print a line starting with `OK:`; failures print a line starting with `ERROR:` with a reason. `HELP` prints the command list. `EXIT` prints `Bye.` These are informational; the specs only require that errors be reported.

### D6. Validation details (resolving source ambiguities)
- **DNI:** length 9, first 8 chars `Character.isDigit` (ASCII digits), last char must equal `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`. Lowercase letters are rejected (the source examples are uppercase).
- **Email:** exactly one `@`; local part non-empty; domain part split at the **last** `.`: name before it non-empty and extension exactly 3 chars. Extension characters must be letters (consistent with `.xxx`/`.com`). No whitespace allowed.
- **Password:** at least one `Character.isUpperCase`, `isLowerCase`, `isDigit`, and one "symbol" = any char that is not a letter, digit or whitespace. No minimum length is imposed (not required by source; `Clave1!` is 7 chars).
- **Card:** length 16 and all chars ASCII digits.
- **Name, surname(s), phone:** must be non-blank (all data required); otherwise stored exactly as given.
- **Update of `NAME`/`SURNAME`/`PHONE`:** new value must be non-blank.
- **Update `EMAIL` to the same email:** accepted (it does not belong to *another* user).
- **Field name:** matched case-sensitively against `UserField` names; `DNI` and unknown names are rejected.

### D7. Persistence
`UserRepository(Path file)`; `loadAll()` returns an empty list if the file is missing, and skips blank lines; `saveAll(Collection<User>)` rewrites the whole file using UTF-8. Writing is done to the target path directly (data volume is tiny). A malformed stored line (wrong field count) is skipped with a warning on stderr rather than aborting startup. The default file is `users.txt` in the working directory; tests pass a temp path. Updates are applied to a copy-free in-memory model only after validation succeeds, then saved; if saving fails with `IOException` the operation reports `ERROR` (in-memory change is rolled back).

### D8. Mode selection and errors
`App.main(args)`: no args → read `System.in`; ≥1 arg → `args[0]` is a file path. File existence/readability is checked and any `IOException` while opening/reading is caught and reported as `ERROR: cannot read file <path>: <reason>` to stderr, then `main` returns normally. A testable `App.run(String[] args, InputStream in, PrintStream out, PrintStream err, Path dataFile)` holds the logic.

## Risks / Trade-offs

- [Passwords stored in plain text] → Explicitly mandated by the source for this delivery (no encryption); documented here.
- [Case-sensitive commands could surprise users] → `HELP` shows exact syntax; behavior is consistent across both modes.
- [Whole-file rewrite on each change] → Acceptable for the expected data volume; simplest correct approach.
- [`users.txt` created in the working directory] → Documented; tests isolate via temp directories so the repository is not polluted.

## Migration Plan

Greenfield; no migration. Rollback is reverting the change.
