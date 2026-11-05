# Design

## Context

Existing: `App` (entry point printing a placeholder), `model.User`, `model.UserValidator` (static checks). Source §3.2 requires separate classes for the user, application start-up, and storing/retrieving users; the main class must not hold file I/O logic. Later milestones add file persistence, a file argument, HELP/EXIT and USER DELETE/UPDATE on top of the same command processing.

## Goals / Non-Goals

**Goals:**
- One command-processing path that later input sources (file argument) reuse unchanged.
- A store abstraction that the persistence milestone can back with a file without touching the command logic.

**Non-Goals:**
- Introducing design patterns (not required in Delivery 1), HELP/EXIT, other USER commands, persistence.

## Decisions

- **`command.CommandProcessor#process(String line) : String`** returns the response text (or `null` for blank lines). `App` owns only the read-print loop over a `BufferedReader` on `System.in`. Returning strings keeps processing testable and identical for any future input source.
- **`repository.UserRepository`** (in-memory, `LinkedHashMap` keyed by DNI to preserve insertion order) with `add`, `findByDni`, `existsByDni`, `existsByEmail`, `findAll`, `size`. The persistence milestone will add load/save here, not in `User` or `App`.
- **Parsing**: `line.split(";", -1)`, each element trimmed; command name = first element (exact, case-sensitive match on `USER CREATE`). `-1` keeps trailing empty elements so a missing final field is detected as a wrong element count rather than silently dropped.
- **Check order**: element count → field validity (DNI, email, password, card; first failing field reported) → DNI uniqueness → email uniqueness → store. Validation before uniqueness so malformed data is reported as such.
- **Response format**: `OK: ...` on success, `ERROR: ...` on failure. The assignment does not prescribe wording; the prefixes give a stable, testable contract.
- **Email uniqueness is exact string comparison.** The source does not mention case folding, so none is invented.
- **Robustness**: `process` catches unexpected `RuntimeException`s and turns them into `ERROR` responses so no command terminates the application.

## Risks / Trade-offs

- [Response wording may need to match a grader's expected text] → Wording is centralised in `CommandProcessor` for easy adjustment.
- [Unrecognised HELP/EXIT until execution-control milestone] → Explicitly deferred; they answer `ERROR` instead of crashing.
