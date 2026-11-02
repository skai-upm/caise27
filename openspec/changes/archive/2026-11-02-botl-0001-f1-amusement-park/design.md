# Design

## Context

Greenfield repository: only `README.md`, `LICENSE`, and the OpenSpec scaffolding exist. See proposal.md for motivation and specs/ for the behavior. Constraints shaping the design:

- Java 21, standard Maven Quickstart layout, `mvn package` must yield `target/nombre-aplicacion.jar`.
- Runtime uses only the Java standard library. No regex, no frameworks, no databases, no encryption.
- User, main, and persistence responsibilities live in separate classes.
- Later deliveries (F2 groups/tickets/attractions, F3 layering) will extend this code, so the command pipeline must be easy to extend.

## Goals / Non-Goals

**Goals:**
- One command pipeline (`CommandProcessor`) shared by interactive and file input.
- Atomic commands: a rejected command or a failed save leaves memory and the file as they were.
- Validation reusable by both create and update.
- Everything testable in-process (injectable streams and file path), without spawning a JVM.

**Non-Goals:**
- Layered packages, design patterns, or dependency injection frameworks (explicitly not required in Delivery 1; arrive in F3).
- Phone validation, password hashing, or encryption (prohibited or not required in Delivery 1).
- Concurrency or multi-process access to the users file.

## Decisions

1. **Coordinates and layout.** `groupId es.upm.etsisi`, `artifactId amusement-park`, package `es.upm.etsisi.park`. `maven.compiler.release=21`. `<finalName>nombre-aplicacion</finalName>`, so the JAR path matches the source text literally. The `maven-jar-plugin` manifest sets `mainClass` to `es.upm.etsisi.park.App`. *Alternative:* keep the default `amusement-park-1.0-SNAPSHOT.jar`. Rejected because the source names `target/nombre-aplicacion.jar` in every run instruction.
2. **Classes.**
   - `User`: encapsulated fields. `dni` is `final` and has no setter. Package-private setters let `UserService` apply validated updates.
   - `UserValidator`: static `isValidDni/isValidEmail/isValidPassword/isValidCard`, written with `charAt`, `Character.*`, `indexOf`, and `split` only.
   - `UserField`: enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
   - `UserRepository`: persistence class. Owns the file path, `load()`, and `save(Collection<User>)`.
   - `UserService`: business rules for create, delete, and update, plus uniqueness. Holds the in-memory map and calls the repository.
   - `CommandProcessor`: parses one line with `split(";", -1)`, dispatches, returns the output text, and signals EXIT. `run(BufferedReader, PrintStream)` serves both modes.
   - `App`: `main`. Picks stdin or the file reader, then delegates to `CommandProcessor`. No file I/O logic of its own beyond opening the command source.
   - *Alternative:* a command class per command. Deferred to F3, where command registries and patterns are requested.
3. **Error model.** `UserService` throws an unchecked `UserException(message)` for business rejections. `CommandProcessor` catches it and any `RuntimeException`, prints `ERROR: <message>`, and continues. Success messages are `USER CREATED`, `USER DELETED`, and `USER UPDATED`, as in source §16.
4. **Atomicity.** Every mutation runs validate → apply in memory → `repository.save(all)`. If the save throws, the in-memory change is reverted (re-insert, restore the previous field value, or remove the created user) and an error is reported. `save` writes to a temp file in the same directory and then calls `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`, falling back to a plain replace if atomic moves are unsupported, so a crash never leaves a half-written file.
5. **Users file location.** `users.txt` in the current working directory. The source does not name a path, so this is the simplest choice that works when instructors run `java -jar target/nombre-aplicacion.jar input.txt`. Tests inject a temp path through the `UserRepository` constructor.
6. **Loading.** A missing file means no users. A line that does not split into exactly 7 fields is skipped, with a warning on stderr, and loading continues. This is a controlled failure for hand-edited files, and the source does not specify the behavior.
7. **Parsing details.** The whole line is trimmed. A blank line is ignored in both modes. Tokens are not trimmed, because surnames contain spaces. The keyword (`USER CREATE`, `HELP`, ...) is matched exactly and case-sensitively, as written in the source. A wrong field count or a blank field gives the error `Malformed command`.
8. **Email rule.** The value contains exactly one `@`. The local part is non-empty. The domain part splits on `.` into at least two segments, all non-empty, and the last segment has exactly three characters. Whitespace anywhere is rejected. This satisfies every source example, including the valid `ana.nueva@correo.com`.
9. **Password symbol.** Any character that is not a letter, not a digit, and not whitespace. `;` can never appear because it is the separator.
10. **EXIT output.** `EXIT` prints `BYE` and stops the loop. End of input without `EXIT` also ends normally. Interactive mode prints no prompt, so both modes produce identical output.
11. **Tests.** JUnit 5 (`junit-jupiter`, test scope only), the Quickstart's testing convention, upgraded from JUnit 4. It is not on the runtime classpath, so the standard-library-only runtime restriction holds. `maven-surefire-plugin` is pinned to a JUnit 5-capable version.

## Risks / Trade-offs

- [Mandatory milestone Git history: 11 commits with exact messages] → **Conflict with the batch harness.** This change is executed by the BOTL batch orchestrator, which owns Git history and creates exactly one commit per proposal after archive. The prescribed messages (`[F0-C01] Initialize Maven project with JDK 21` … `[F1-C09] Complete delivery 1 integration`) are therefore **not** produced as separate commits here. The checkpoint order is still followed in tasks.md, with build and tests verified after each checkpoint. Flagged, not silently resolved: a student submission would need these commits.
- [Source example line breaks: `Clave1` / `!` split across lines] → Treated as a typesetting artifact. The intended password is `Clave1!`, confirmed by the later `USER UPDATE;ana@correo.com;Clave1!;...` examples.
- [`target/nombre-aplicacion.jar` may be a placeholder ("application name")] → Taken literally via `finalName`, the safest reading of a contractual path.
- [Blank fields rejected on create/update] → The source does not explicitly forbid empty names. Rejecting them prevents unparseable stored lines and is recorded here as an interpretation.
- [Plain-text passwords in `users.txt`] → Required by Delivery 1, where encryption is prohibited. F3 introduces BCrypt.
- [Source requires DNI letter to be uppercase?] → Unspecified. Only uppercase letters matching the algorithm are accepted, since the table is uppercase.

## Migration Plan

Not applicable (new project). Rollback is reverting the commit.
