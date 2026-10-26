# Design

## Context

The code base is the bare quickstart project (`es.upm.parque.App` printing "Hello World!"). See proposal.md for motivation. The epic forbids regex, external libraries, frameworks and design patterns introduced only for future needs, and mandates at least three separate responsibilities: user state, main/terminal interaction, file storage.

## Goals / Non-Goals

**Goals:**
- Small set of cohesive classes that satisfy the mandated separation.
- Command handling that is testable without a terminal (input string → output string).

**Non-Goals:**
- HELP/EXIT commands and file input mode (next epic).
- Interfaces or patterns for persistence; hashing of passwords.

## Decisions

- **Classes (package `es.upm.parque`):**
  - `User` – encapsulated state; `dni` is `final` with no setter; setters for editable fields.
  - `UserField` – enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`, with a lookup that returns `null` for unknown names (so `DNI` is rejected as an invalid field).
  - `UserValidator` – static validation methods for DNI, email, password, card (loops and `String.split`, no regex).
  - `UserRepository` – reads/writes the `;`-separated file; receives/returns `User` objects; knows nothing about validation. Missing file = no users.
  - `UserManager` – business rules: create/delete/update, uniqueness, revalidation; holds the in-memory list and calls the repository to save after each change. Signals failures by throwing an unchecked `UserException` carrying the message, keeping success paths simple.
  - `CommandProcessor` – splits each line with `split(";", -1)`, dispatches on the operation, checks parameter counts, and returns the response string; catches `UserException` to produce `ERROR <message>`.
  - `App` – builds the objects with file `data/users.txt` and loops over `System.in` printing each response.
  Alternative: put business rules in `CommandProcessor`. Rejected: mixing parsing with rules makes both harder to test and the main/terminal separation weaker.
- **Split with limit -1** so trailing empty fields are counted and reported as empty instead of silently dropped.
- **Full rewrite of the file on each change.** Simple and consistent for small data; avoids partial-line edits.
- **Output format:** `OK USER CREATED|DELETED|UPDATED` and `ERROR <reason>`; one line per command.
- **Storage location:** `data/users.txt` relative to the working directory, directory created on first save. `data/` is git-ignored.

## Risks / Trade-offs

- [Values cannot contain `;`] → inherent in the command/file format; such input is split into extra fields and rejected by the parameter count.
- [Corrupt lines in the data file] → lines without exactly 7 fields are skipped on load rather than crashing startup.
- [Plaintext passwords] → explicitly required for this epic; encryption is a later delivery.
