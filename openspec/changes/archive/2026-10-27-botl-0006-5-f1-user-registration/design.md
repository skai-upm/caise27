# Design

## Context

Existing: `CommandProcessor` (HELP/EXIT, unknown-command error), `User`, `UserRepository` (in memory, keyed by email), `UserValidator` (F1-C02). The source leaves to the students how classes cooperate to interpret, validate and create.

## Goals / Non-Goals

**Goals:**
- Parse → validate → uniqueness → store, each step in the class that owns it.

**Non-Goals:**
- USER DELETE/UPDATE (F1-C05/F1-C06), persistence (F1-C04).

## Decisions

- **Parsing in `CommandProcessor`:** `line.split(";", -1)` (single-character literal split; `-1` keeps trailing empty fields so they count as elements). The first element is the command name; `USER CREATE` requires exactly 8 elements.
- **`UserService.createUser(...)`:** runs `UserValidator.validate`, then checks `UserRepository.existsByDni` / `findByEmail`, then saves. Failures throw `UserOperationException` with a message; `CommandProcessor` prints `ERROR: <message>` and continues. This keeps business rules out of the command interpreter and reusable by later commands.
- **Output:** success prints `USER CREATED` (as in the source example); errors are prefixed `ERROR:`. The source allows message wording to vary.
- **Exact comparison (ambiguity, flagged):** DNI and email uniqueness compare the strings exactly as given; the source does not say whether email comparison is case-insensitive.
- **Example line break (flagged):** the source example shows `Clave1` and `!` on separate lines; this is treated as a wrapping artifact of the one-line command `...;Clave1!;...`, since commands are one per line.
- **Empty names (not validated):** the source defines no rule for first name/surnames, so none is invented.
- **Mandatory commit:** `[F1-C03] Implement user creation` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Field values are not trimmed] → Values are stored as written between separators, matching "basic String operations"; surrounding whitespace in DNI/email/card will fail validation instead of being silently fixed.
