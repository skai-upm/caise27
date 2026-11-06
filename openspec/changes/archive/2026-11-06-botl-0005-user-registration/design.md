# Design

## Context

See proposal.md – Why. Today `Main` prints a banner; `User` and `UserValidator` exist in `es.upm.park.model`. The source asks students to decide how classes cooperate to interpret the request, validate and create the user, and forbids the model or main class from taking unrelated responsibilities (later stressed by F1-C04).

## Goals / Non-Goals

**Goals:**
- Separate input reading (Main), command interpretation (command layer), business rules (service) and storage (in-memory repository), so later milestones (persistence, delete/update, file input, HELP/EXIT) extend rather than rewrite.

**Non-Goals:**
- Persistence, file input, HELP/EXIT, delete/update.

## Decisions

- **`CommandProcessor` (package `es.upm.park.command`)**: `String process(String line)` splits with `line.split(";", -1)`, trims only the command name, dispatches on it, and returns the message to print. It never throws for bad input: domain errors arrive as `UserException` and are turned into `Error: ...` messages. Alternative: one class per command now — deferred until more commands exist, to keep this increment small.
- **`UserService` (package `es.upm.park.service`)**: `User create(...)` validates fields in the order DNI, email, password, card using `UserValidator`, then checks DNI and email uniqueness against the repository, then stores. Failures throw `UserException` (checked-free `RuntimeException` subclass) with a field-specific message.
- **`UserRepository` (package `es.upm.park.repository`)**: in-memory `List<User>` with `findByDni`, `existsByEmail`, `add`, `findAll` (unmodifiable). Persistence will plug in here in F1-C04.
- **`Main`**: prints banner, then wires `UserRepository → UserService → CommandProcessor` and runs `run(BufferedReader, PrintStream)` over `System.in`. Reading is in a method taking reader/stream so tests can drive it without touching the real console.
- **Fields are not trimmed**: values are stored exactly as written; only the command name is trimmed. Email uniqueness compares exact strings.
- **Messages**: success `User created: <DNI>`; errors `Error: invalid DNI|email|password|card number`, `Error: a user with DNI <dni> already exists`, `Error: a user with email <email> already exists`, `Error: USER CREATE expects 7 fields`, `Error: unknown command '<name>'`.

## Risks / Trade-offs

- [Source does not fix message wording] → wording chosen here and specified only by content (field named, DNI echoed), not exact text.
- [Source lists `USER DELETE`/`USER UPDATE`/`HELP`/`EXIT` as recognized commands] → until their milestones they are answered as unknown commands; their milestones will add them.
- [Email uniqueness case sensitivity unspecified] → exact match; revisit if a later milestone states otherwise.
