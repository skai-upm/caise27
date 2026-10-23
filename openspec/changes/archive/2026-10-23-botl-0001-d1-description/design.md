# Design

## Context

Greenfield: the repository holds only a README, LICENSE, `.gitignore` (already Maven-aware) and OpenSpec scaffolding. Constraints from the assignment (see proposal.md): Java 21, Maven quickstart structure, executable JAR, Java standard library only at runtime, no regular expressions, no databases/frameworks/encryption, and a strict separation between the user class, the main class and the persistence class. Subsequent deliveries will add park features, so command dispatch must be easy to extend.

## Goals / Non-Goals

**Goals:**
- One command-processing path shared by interactive and file mode.
- Validation logic reusable by both create and update.
- Persistence isolated behind a small repository class so its format can evolve later.
- Unit tests for validation, user operations, persistence and command processing; an end-to-end check of the built JAR.

**Non-Goals:**
- Reproducing the course's mandatory GitLab milestone commit history (the batch orchestrator owns commits).
- Hashing/encrypting passwords or phone numbers (explicitly not required; encryption forbidden in this delivery).
- Concurrency or multi-process access to the users file.

## Decisions

### Project layout and build
- Generated from `maven-archetype-quickstart` 1.5 (groupId `es.upm.etsisi`, artifactId `amusement-park`, package `es.upm.etsisi.park`), then adjusted: `maven.compiler.release=21`, `<finalName>amusement-park</finalName>`, and `maven-jar-plugin` manifest `Main-Class: es.upm.etsisi.park.App`. Since there are no runtime dependencies, a plain JAR with a manifest is sufficient; a shade/assembly plugin is unnecessary.
- The assignment's `nombre-aplicacion.jar` is a placeholder; `amusement-park.jar` is the concrete name.
- JUnit Jupiter (test scope) comes with the archetype; it never reaches the runtime JAR, so the "standard library only" rule holds for the application.

### Class responsibilities
| Package | Class | Responsibility |
|---|---|---|
| `es.upm.etsisi.park` | `App` | Entry point: selects input source (stdin or file), wires components, runs the loop, handles missing file. No file-format or business logic. |
| `...park.cli` | `CommandProcessor` | Parses one line (`split(";", -1)`), dispatches by keyword, prints results/errors. Returns whether processing should continue (false after `EXIT`). |
| `...park.user` | `User` | Encapsulated data; `dni` is `final` with no setter. |
| `...park.user` | `UserField` | Enum of modifiable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD. |
| `...park.user` | `UserValidator` | Static checks for DNI, email, password, card, non-empty text; no regex. |
| `...park.user` | `UserService` | Create/update/delete with uniqueness and authentication; persists through the repository after each successful change; throws `UserException` with a user-facing message on failure. |
| `...park.persistence` | `UserRepository` | Loads/saves the list of users in the `;`-separated text format. |

Alternative considered: putting dispatch in `App` with a `switch`. Rejected because both input modes must share one implementation and later deliveries will add more commands; a dedicated processor keeps `App` thin.

### Parsing and validation without regex
- Command lines are split with `line.split(";", -1)` (a literal one-character delimiter, as the assignment explicitly allows `split`); the `-1` limit keeps trailing empty fields so `USER CREATE;...;` with an empty card is reported as invalid rather than as a wrong argument count.
- Keyword: first element trimmed, inner whitespace normalised by splitting on spaces and rejoining non-empty tokens, then upper-cased, so `user  create` and `USER CREATE` match.
- Email: `split("@", -1)` must give exactly two non-empty parts; the domain part is scanned with `indexOf`/`lastIndexOf('.')` — every dot-separated segment non-empty, at least one dot, last segment exactly three letters; no whitespace anywhere.
- DNI: length 9, first 8 chars `Character.isDigit`, letter compared to `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)`; normalised to upper case.
- Password: single pass with `Character.isUpperCase/isLowerCase/isDigit`; a symbol is any other non-whitespace char.
- Card: length 16 and all digits.

### Persistence
- File path: `users.txt` in the working directory, overridable with system property `park.users.file` (used by tests to avoid touching real data).
- Save strategy: rewrite the whole file to a sibling temp file then `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)` (fallback to non-atomic move if unsupported), so a crash mid-write does not corrupt existing data. UTF-8 encoding.
- The `UserService` saves before reporting success; if saving throws `IOException`, the in-memory change is rolled back and an error is printed.
- Field values cannot contain `;` because they arrive through `;`-split commands, so the format needs no escaping.

### Output conventions
- Successes: `USER CREATED`, `USER UPDATED`, `USER DELETED` (matching the assignment example).
- Failures: a single line starting with `ERROR: `.
- `EXIT` prints `BYE` and stops the loop. All command output goes to standard output so interleaving is deterministic; only start-up failures (missing command file) go to standard error with exit status 1.
- Input arguments are trimmed; this means a password cannot start or end with a space (documented trade-off).

## Risks / Trade-offs

- [Passwords are stored in plain text] → Explicitly required by the assignment (no encryption in delivery 1); revisit in a later delivery.
- [Trimming arguments changes values with leading/trailing spaces] → Accepted; such values are almost always input mistakes and the behaviour is consistent across create/update/delete.
- [Whole-file rewrite on every change is O(n)] → Fine for course-scale data; the repository class isolates the format if this changes.
- [Example commands in the assignment are line-wrapped (`Clave1` / `!`)] → Treated as a document-formatting artefact; each command is one physical line.
