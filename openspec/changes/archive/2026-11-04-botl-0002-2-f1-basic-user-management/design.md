# Design

## Context

The codebase is the archetype skeleton from change 1 (`es.upm.park.App`, JUnit 5 via the archetype BOM, Java 21 release). See proposal.md for motivation. Constraints: Java standard library only, no regular expressions for validation, no frameworks, no speculative patterns or layers.

## Goals / Non-Goals

**Goals:**
- Minimal set of cohesive classes meeting the required separation (user / main / persistence) plus the validation and command handling the specs need, each unit-testable without the terminal.

**Non-Goals:**
- HELP/EXIT commands and file-driven command execution (next change), encryption, other domain entities, interfaces or repository abstractions.

## Decisions

- **Classes (package `es.upm.park`):**
  - `User` — private fields, getters; setters only for editable fields; no DNI setter. A copy constructor supports atomic updates.
  - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed from the command with `UserField.valueOf` guarded against `IllegalArgumentException`.
  - `UserValidator` — static, side-effect-free checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNonBlank`). Reused by both CREATE and UPDATE so rules exist in one place.
  - `UserFileStore` — `List<User> load()` / `void save(List<User>)` for a given `Path`; the only class doing file I/O. Lines split with `split(";", -1)`; blank lines ignored; lines with a field count other than 7 are skipped (reported on stderr) so a corrupt line cannot crash startup.
  - `UserManager` — in-memory `List<User>` loaded from the store at construction; `create`, `delete`, `update` return a `Result` (success flag + message). Uniqueness and authentication are checked here.
  - `CommandProcessor` — splits a line, dispatches `USER CREATE|DELETE|UPDATE` to `UserManager`, checks parameter counts, and converts every failure into an `ERROR:` message; never throws for bad input.
  - `App` — reads stdin lines with `BufferedReader`, passes each non-blank line to `CommandProcessor`, prints the result; data file `users.txt` in the working directory.
- **Parsing:** `line.split(";", -1)` (limit -1 keeps trailing empty fields so `USER DELETE;a@b.com;` is caught as an empty parameter). Operation part split with `split(" ")` after trimming, ignoring empty tokens. Each parameter is trimmed. Keywords are matched case-sensitively in uppercase, matching the documented formats.
- **Validation without regex:** DNI via length check, `Character` digit range checks and `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)`; email via `split("@", -1)` (exactly two nonempty parts) and `lastIndexOf('.')` for domain/extension (avoids escaping `.` in a split pattern); password via a loop over characters with `Character.isUpperCase/isLowerCase/isDigit` and "symbol = not letter/digit/whitespace"; card via length 16 and `'0'..'9'` checks.
- **Atomicity:** every mutating operation works on a copy (new list / copied `User`), saves it through `UserFileStore`, and only then replaces the in-memory state. A failed save (IOException) leaves memory and the previous file unchanged and returns an error. Saves write a temporary sibling file and move it over the target (`ATOMIC_MOVE` when supported).
- **Email comparisons are case-insensitive** for uniqueness and lookup (assumption recorded; the source does not specify case handling). Stored values keep the case supplied.
- **Messages:** results start with `OK:` or `ERROR:` followed by a short human-readable reason.

## Risks / Trade-offs

- [`;` inside a value would corrupt the record] → impossible by construction: values come from a `;`-split command.
- [Plain-text passwords] → explicitly accepted by the source for this delivery.
- [Trimming parameters] → a password with leading/trailing spaces cannot be set; recorded as an assumption, consistent with the space-free examples.
