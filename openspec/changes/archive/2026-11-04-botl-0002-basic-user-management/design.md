# Design

## Context

The code base is the Quickstart skeleton: `es.upm.park.App` (prints "Hello World!") and `AppTest`. Specs: `application-bootstrap`. Only the Java standard library may be used; no regular expressions.

## Goals / Non-Goals

**Goals:**
- Minimal class set that satisfies the required separation (user / main class / storage) while keeping command logic testable without a terminal.

**Non-Goals:**
- Interfaces, repositories abstractions, layers or patterns for future deliveries; HELP/EXIT; file-input mode.

## Decisions

- **Classes (all in `es.upm.park`):**
  - `User` — encapsulated state; `dni` is `final` with no setter; setters for the six editable fields.
  - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
  - `UserValidator` — static pure checks (`isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard`, `isNonBlank`), reused by both CREATE and UPDATE so the rules exist once.
  - `UserFileStorage` — `List<User> load()` / `void save(List<User>)` for the semicolon file; the only class doing file I/O.
  - `UserManager` — in-memory list loaded from storage at construction; uniqueness checks, authentication, create/delete/update; saves the full list after each successful mutation.
  - `CommandProcessor` — splits a line with `String.split(";", -1)` and dispatches to `UserManager`, returning an `OK ...`/`ERROR ...` message; catches unexpected exceptions so a bad line never terminates the program.
  - `App` — wires the storage file (`users.txt` in the working directory, overridable by the first program argument), reads `System.in` line by line, prints results.
  Alternative of putting parsing in `App` rejected: it would make command behaviour testable only through stdin.
- **Atomicity:** each mutation validates everything first, then applies to a copy of the affected user/list, saves, and only then replaces the in-memory state. If saving throws, memory is left unchanged and an `ERROR` is returned. Alternative (mutate then save) rejected: a failed write would desynchronise memory and file.
- **Parsing:** `split(";", -1)` keeps trailing empty fields so `USER DELETE;a@b.com;` is a parameter error rather than silently missing. The operation token is trimmed and compared case-sensitively against `USER CREATE|DELETE|UPDATE` (the documented format). Parameter values are used as given (not trimmed) except the field name, which is upper-cased with `Locale.ROOT` before enum lookup.
- **Uniqueness:** DNI compared exactly (validated DNIs are already uppercase); email compared with `equalsIgnoreCase`. Email-update uniqueness ignores the user being updated.
- **Persistence format:** fixed order `name;surnames;dni;email;password;phone;card`. Lines with a field count other than 7 are skipped on load (blank lines ignored). Values cannot contain `;` because the command itself is split on `;`.
- **Validation details without regex:** DNI uses `Character` checks on ASCII digits and the 23-letter table; email uses `split("@", -1)` (exactly two nonempty parts) and `lastIndexOf('.')` in the domain (nonempty name, 3-character extension), rejecting whitespace; password scans characters with `Character.isUpperCase/isLowerCase/isDigit` and treats any other non-whitespace character as a symbol; card checks length 16 and ASCII digits.

## Risks / Trade-offs

- [Passwords stored in plaintext] → Explicitly required for this delivery; encryption is a later change.
- [Malformed lines in a hand-edited file are skipped] → Keeps startup robust; acceptable because the file is application-owned.
- [Spec `application-bootstrap` "No domain functionality in bootstrap"] → It constrains the bootstrap change only; not modified here. Flagged for clarification if it is read as a permanent constraint.
