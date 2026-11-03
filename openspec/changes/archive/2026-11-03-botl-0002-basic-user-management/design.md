# Design

## Context

The code base is the Quickstart skeleton from change 1 (`es.upm.etsisi.amusementpark.App`). See proposal.md for scope. Only the Java standard library may be used; no regular expressions.

## Goals / Non-Goals

**Goals:** the minimal set of classes that satisfies the required responsibility split, with each validation rule reusable by CREATE and UPDATE.

**Non-Goals:** interfaces, patterns or layers not required by the epic; HELP/EXIT; reading commands from files.

## Decisions

All classes in package `es.upm.etsisi.amusementpark`:

- **`User`** – encapsulated state; `dni` is `final` with no setter; setters for the six editable fields. No I/O.
- **`UserField`** (enum) – `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed from the command token (case-insensitive, trimmed). `DNI` is deliberately absent.
- **`UserValidator`** – static, side-effect-free checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNotBlank`), used by both creation and update so rules are defined once.
  - DNI: length 9, first 8 chars `'0'..'9'`, `TRWAGMYFPDXBNJZSQVHLCKE.charAt(n % 23)` equals the last char.
  - Email: `split("@", -1)` must yield exactly 2 non-empty parts; in the domain part, `lastIndexOf('.')` splits domain (non-empty) and extension (length 3). `String.split` is used for `@` as required; `.` handling uses index operations to avoid regex metacharacters.
  - Password: loop over chars with `Character.isUpperCase/isLowerCase/isDigit`; symbol = not letter/digit/whitespace.
  - Card: length 16, every char `'0'..'9'`.
- **`UserFileStorage`** – the only class touching the file: `List<User> load()` (missing file → empty list; blank lines skipped; malformed lines skipped with a warning on stderr) and `void save(List<User>)` (rewrites the whole file). Path injected via constructor (`users.txt` by default).
- **`UserService`** – holds the in-memory `List<User>` loaded from storage, checks uniqueness (DNI exact, email case-insensitive), authentication (email lookup case-insensitive, password exact) and applies create/update/delete. Throws `IllegalArgumentException` with a message on any rule violation. Each successful mutation calls `storage.save(users)`; if saving fails the in-memory change is reverted so memory and file stay consistent.
- **`CommandProcessor`** – parsing boundary: `line.split(";", -1)`, then the first token `split(" ")` gives the operation (`USER` + `CREATE|DELETE|UPDATE`); checks the exact parameter count (8/3/5 tokens), delegates to `UserService`, and converts results/exceptions into a one-line response `OK: ...` / `ERROR: ...`. It never throws for bad input.
- **`App`** – startup and terminal interaction only: builds storage → service → processor, reads lines from `System.in` until end of input, prints each response. No file logic.

Uniqueness is checked in `UserService` before any change; validation of all fields happens before the `User` is constructed, so a rejected command never creates a partial user. File updates follow only successful operations.

**Assumptions recorded:** field values are trimmed; email uniqueness/lookup is case-insensitive; the DNI letter must be uppercase; first name, surnames and phone must be non-blank; a new email equal to the user's own current email is allowed. Output wording is not prescribed by the source and is chosen here.

## Risks / Trade-offs

- [Plain-text passwords] → accepted for this delivery; encryption is a later deliverable.
- [Values cannot contain `;`] → inherent in the command and file format.
- [Whole-file rewrite on each change] → trivial for expected sizes; keeps the format simple.
