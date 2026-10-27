# Proposal

## Why

Milestone F1-C03 makes the application accept its first real command, `USER CREATE`, adding users that satisfy every validation rule from F1-C02 while keeping DNI and email unique. Until now input lines are read but ignored.

## What Changes

- Introduce command interpretation: each input line is split on `;` with basic String operations; an unrecognised or malformed command produces an error message and never terminates the application.
- Add `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`: validates DNI, email, password and card (reusing `UserValidator`), rejects duplicate DNI or email, and stores the user, answering `USER CREATED`.
- Add the class responsible for storing and retrieving users (in memory for now; file persistence is F1-C04).
- Route both input modes (stdin and file) through the same command processor, writing responses to standard output.
- Commit with the exact message `[F1-C03] Implement user creation`.

Not included: `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT` (later milestones); until then they are reported as unknown commands.

## Capabilities

### New Capabilities
- `user-registration`: The `USER CREATE` command — syntax, validation, uniqueness and responses.
- `command-processing`: General command interpretation — `;`-separated syntax, handling of unknown or malformed commands.

### Modified Capabilities
<!-- none -->

## Impact

- New `command/CommandProcessor`, `repository/UserRepository`; `Main` now prints processor output.
- Ambiguities flagged: command keywords are matched case-sensitively as written (`USER CREATE`); fields are trimmed of surrounding blanks; all seven fields must be non-empty; email uniqueness compares exact text; when several fields are invalid the first failing check (DNI, email, password, card, then uniqueness) is reported. Exact message wording is free per the source ("exact output messages may vary").
