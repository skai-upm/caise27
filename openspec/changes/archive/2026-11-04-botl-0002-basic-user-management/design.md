# Design

## Context

The codebase is the archetype skeleton from change 1: `es.upm.etsisi.amusementpark.App` (Hello World) and a placeholder JUnit 5 test. Only the Java standard library may be used; no regular expressions, frameworks or extra layers.

## Goals / Non-Goals

**Goals:**
- Minimum class split required by the source: user representation, main class, persistence class; plus a small number of focused helpers so each class has one responsibility and is unit-testable.
- Every command is all-or-nothing: validate first, mutate second, persist third.

**Non-Goals:**
- HELP/EXIT and reading commands from a file (next change).
- Interfaces, design patterns or layers that the source does not require.
- Encryption or hashing of passwords (a later delivery).

## Decisions

### Classes (all in `es.upm.etsisi.amusementpark`)
| Class | Responsibility |
|---|---|
| `User` | Encapsulated state. `dni` is `final` with no setter; other fields have getters/setters. No I/O, no validation. |
| `UserField` | Enum of editable fields `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`. DNI is deliberately absent. |
| `UserValidator` | Static, side-effect-free checks: `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNotBlank`. Reused by both CREATE and UPDATE. |
| `UserFileRepository` | Loads `List<User>` from and saves `List<User>` to a `Path`. Only class touching the file. |
| `UserManager` | Holds the in-memory users (loaded from the repository at construction), performs create/delete/update with uniqueness and authentication checks, and saves through the repository after each successful mutation. Throws `UserException` (checked message carrier) on rejection. |
| `UserException` | Exception with a user-facing message for rejected operations. |
| `CommandProcessor` | Splits a command line with `String.split(";", -1)`, dispatches on the operation token, checks parameter counts, calls `UserManager`, and turns outcomes into `OK: ...` / `ERROR: ...` strings. Never throws. |
| `App` | Builds the repository (file `users.txt` in the working directory), manager and processor; reads standard input line by line and prints each result. Contains no file-format logic. |

`UserManager` and `CommandProcessor` are added beyond the strict minimum because putting command parsing or business rules into `App` would make the main class untestable and blur the responsibilities the source asks to separate. They are concrete classes, with no interfaces.

### Parsing boundaries
`line.split(";", -1)` keeps trailing empty fields, so `USER DELETE;a@b.com;` is seen as 2 parameters with an empty password, not 1. Parameters are trimmed. The operation token is `parts[0].trim()`; inner whitespace is collapsed by `split(" ")` of the token, ignoring empty pieces, so `USER  CREATE` is accepted. Exact parameter counts: CREATE 7, DELETE 2, UPDATE 4.

### Validation details (no regex)
- DNI: length 9, chars 0..7 in `'0'..'9'`, last char equals `"TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23)`. Lowercase letters are rejected (assumption: canonical uppercase form).
- Email: `split("@", -1)` must yield exactly 2 non-empty parts (a literal one-character split pattern, no regex features). In the domain part, `lastIndexOf('.')` must be > 0 and the extension after it must have length 3.
- Password: scan chars with `Character.isUpperCase/isLowerCase/isDigit`; symbol = not letter-or-digit and not whitespace.
- Card: length 16 and each char in `'0'..'9'` (avoids non-ASCII digits that `Character.isDigit` accepts).

### Uniqueness and lookup
Uniqueness is checked in `UserManager` against the in-memory list, which always mirrors the file. DNI comparison is exact; email comparison and email lookup ignore case (assumption: emails are case-insensitive identifiers). Updating EMAIL to the user's own current email is allowed.

### Persistence flow
The in-memory list is the working copy. On each mutation, `UserManager` applies the change, calls `repository.save(users)` (rewriting the whole file), and if saving throws `IOException` it restores the previous state and reports an error, so memory and file never diverge. The file is UTF-8. Lines that do not have exactly 7 fields are skipped with a warning on standard error when loading.

### Output format
`OK: <message>` or `ERROR: <reason>`, one line per non-blank command.

## Risks / Trade-offs

- [Field values containing `;` would corrupt the file] → impossible via commands, since `;` is the command separator.
- [Plaintext passwords in `users.txt`] → explicitly accepted by the source for this delivery; `users.txt` is added to `.gitignore` so user data is never committed.
- [Whole-file rewrite per change] → acceptable for the expected data volume; simplest correct approach.
