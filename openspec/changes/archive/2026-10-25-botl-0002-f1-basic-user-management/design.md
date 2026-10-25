# Design

## Context

The code base is the archetype skeleton: `es.upm.parque.App` prints a greeting, `AppTest` is a placeholder, the build targets Java 21 and produces `target/parque.jar`. The epic forbids regular expressions, external libraries, databases, frameworks and patterns introduced only for future needs, and requires `User`, the main class and the persistence class to be separate.

## Goals / Non-Goals

**Goals:**
- Small, testable classes with one responsibility each, all in package `es.upm.parque`.
- Every behavior in the specs covered by JUnit tests that do not touch the real `data/` directory.

**Non-Goals:**
- `HELP`, `EXIT` and command files (next proposal), layered packages, interfaces or design patterns.

## Decisions

- **Classes.**
  - `User`: encapsulated state; `dni` is `final` with no setter; setters only for editable fields.
  - `UserField` (enum): `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; `UserField.parse(String)` returns `null` for unknown names (so `DNI` is rejected).
  - `UserValidator`: static `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNotBlank` using `charAt`, `Character` methods, `indexOf/lastIndexOf` and `split("@", -1)`; no `matches`/`Pattern`.
  - `UserRepository`: constructed with a `Path`; `List<User> load()` and `void save(Collection<User>)` (rewrites the whole file, creating parent directories). It knows the line format; `User` does not.
  - `UserManager`: holds the users in memory (`LinkedHashMap` keyed by DNI, preserving insertion order) loaded from the repository; implements create/delete/update rules and saves after each successful change. Returns a response string.
  - `CommandProcessor`: `String process(String line)`: splits with `split(";", -1)`, trims, dispatches on the upper-cased operation name, checks parameter counts, and catches unexpected runtime exceptions into `ERROR ...`.
  - `App`: `main` wires `UserRepository(Path.of("data", "users.txt"))` → `UserManager` → `CommandProcessor` and calls `run(BufferedReader, PrintStream)` that loops over lines until end of input. `run` is a separate method so it is testable with in-memory streams.
  Alternative: put the rules in `CommandProcessor` — rejected because parsing and business rules would mix, and later epics add many commands.
- **Validation order for CREATE**: parameter count → non-empty name/surname/phone → DNI → email → password → card → duplicate DNI → duplicate email. The first failing rule determines the error message.
- **Error messages** are fixed strings (`ERROR INVALID DNI`, `ERROR DUPLICATE EMAIL`, …) so tests and the spec reference them exactly. A wrong password and an unknown email give the same `ERROR INVALID CREDENTIALS` to avoid revealing which emails exist.
- **Save strategy**: rewrite the whole file after each change (`Files.write` with UTF-8). Simple and correct for the expected size. If writing fails, the response is `ERROR STORAGE FAILURE`.
- **Email comparison** ignores case for uniqueness and lookup; the value is stored as entered.
- **`data/` is added to `.gitignore`** so runtime data is never committed.

## Risks / Trade-offs

- [A value containing `;` cannot be expressed] → By construction `split(";")` separates it; accepted limitation of the command format and the file format.
- [In-memory state and file diverge if a write fails] → Reported as `ERROR STORAGE FAILURE`; acceptable for a laboratory application.
- [Rewriting the file each time is O(n)] → Negligible for park-sized data.
