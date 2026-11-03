# Design

## Context

Starting point: `com.parque.App` (archetype hello-world) and `AppTest`; JUnit 5 is available for tests; Java 21; no runtime dependencies. The source forbids regex, external libraries, frameworks, databases and speculative patterns/layers, and requires three separated responsibilities: user state, main/terminal, file storage.

## Goals / Non-Goals

**Goals:**
- Minimal set of classes that keeps User, App and file storage separated and is unit-testable without the terminal.
- All-or-nothing commands: nothing in memory or on disk changes on rejection.

**Non-Goals:**
- HELP/EXIT, script-file execution (next change), interfaces, layered packages (a later delivery reorganises into layers), encryption.

## Decisions

- **Classes (all in `com.parque`):**
  - `User` — private fields, getters; setters for editable fields only; DNI `final`, set in the constructor.
  - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed from the command with an exact-name lookup (unknown → rejection).
  - `UserValidator` — static, side-effect-free checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNonBlank`) using `charAt`, `indexOf`, `lastIndexOf`, `split("@", -1)` and `Character` tests only.
  - `UserRepository` — reads/writes the `;` file from a `Path` given in the constructor (`load(): List<User>`, `save(List<User>)`). Knows the line format; `User` does not.
  - `UserService` — holds the in-memory list loaded from the repository; implements create/delete/update, uniqueness and authentication; returns a message or throws `IllegalArgumentException` with the rejection reason.
  - `CommandProcessor` — `process(String line): String`; splits on `;` (`split(";", -1)` so trailing empty fields are counted), splits the first token on spaces into `USER` + operation, checks exact parameter count, delegates to `UserService`, converts outcomes to `OK: …` / `ERROR: …`. Catches every runtime exception so a command can never terminate the loop.
  - `App` — wires `UserRepository(Path.of("users.txt"))` → `UserService` → `CommandProcessor`, reads `System.in` line by line, prints each response, exits when input ends. Contains no file I/O for users.
  - Alternative considered: parsing inside `App` — rejected because it mixes terminal I/O with parsing and makes the command contract hard to test.
- **Validation reuse:** CREATE validates every field with `UserValidator` before constructing a `User`; UPDATE maps the field to the same validator method (`NAME`/`SURNAME`/`PHONE` → non-blank, `EMAIL` → email + uniqueness excluding the user itself, `PASSWORD` → password, `CARD` → card).
- **Uniqueness** is checked in `UserService` against the in-memory list (DNI exact match; email exact match, case-sensitive as stored).
- **Persistence/atomicity:** after an operation succeeds in memory, `UserService` calls `repository.save(all users)` which rewrites the whole file. Mutations are applied to a copy first (new list / cloned values); if saving throws an `IOException` the in-memory state is restored and the command reports `ERROR:`. Rewriting the whole file is acceptable for the expected data volume.
- **Load:** missing file → empty list; lines that do not have exactly 7 fields are skipped (reported on standard error) so that a corrupt line cannot crash startup.
- **DNI letter:** `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`; uppercase required (assumption).

## Risks / Trade-offs

- [Plain-text passwords on disk] → Explicitly accepted by the source for this delivery; a later delivery adds hashing.
- [Fields containing `;` would corrupt the file] → Impossible via commands, since `;` is the command separator.
- [Whole-file rewrite on each change] → Simple and atomic enough for a teaching-scale data set.
