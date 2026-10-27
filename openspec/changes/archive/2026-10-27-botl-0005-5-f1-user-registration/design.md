# Design

## Context

`Main` reads lines from stdin or a file and ignores them; `User` and `UserValidator` exist. Source §3.2 requires separate classes for the user, application start-up and user storage.

## Goals / Non-Goals

**Goals:** one command processor shared by both input modes; a storage class; USER CREATE end to end.
**Non-Goals:** file persistence (F1-C04), other USER commands, HELP/EXIT, blank-line policy (F1-C07).

## Decisions

- **`repository/UserRepository`** — in-memory `LinkedHashMap<String dni, User>` with `add`, `findByDni`, `findByEmail`, `existsDni`, `existsEmail`, `size`. Persistence will be added behind this class so callers do not change.
- **`command/CommandProcessor`** — `String process(String line)` returns the response text (or `null` for no output). It splits on `";"` with `split(";", -1)` (literal single-character split; trailing empty fields kept so missing values are detected), trims elements and dispatches on the first element. Keeps `Main` free of business logic and testable without I/O.
- **Error responses** start with `ERROR: ` and are written to standard output so file runs show them inline with successes.
- **Validation order**: field count/emptiness → DNI → email → password → card → DNI uniqueness → email uniqueness.
- **`Main.run(args, in, out, err)`** gains an output stream; each processed line's response is printed.

## Risks / Trade-offs

- [`String.split` takes a regex argument] → `";"` is a literal single char that the JDK handles on its non-regex fast path; this matches the source's explicit permission to use split.
