# Proposal

## Why

Milestone F1-C04: users currently live only in memory and are lost when the application ends. They must be preserved between runs in a text file using the assignment's line format, without letting the user model or the main class take on file-handling responsibilities.

## What Changes

- Store users in a text file, one user per line, attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card.
- Load previously stored users when the application starts, so DNI/email uniqueness also applies to them.
- Write the file whenever the set of users changes (currently: after a successful `USER CREATE`).
- Implement reading/writing in a dedicated persistence class, separate from `User` and `App`; no databases, persistence frameworks or external libraries.
- Commit with the mandatory message `[F1-C04] Add user persistence`.

## Capabilities

### New Capabilities
- `user-persistence`: Storage of users between runs — file format, loading at startup and saving on changes.

### Modified Capabilities
- None (registration rules are unchanged; they now also see loaded users).

## Impact

- New `persistence` package; `UserService` saves through it; `App` loads stored users into the repository at startup (delegating all file logic).
- Ambiguities flagged:
  - File name/location are not specified: `users.txt` in the current working directory is used.
  - Handling of a corrupt line is not specified: such a line is skipped and reported on standard error rather than aborting startup.
  - If saving fails, the operation is reported as an error and the in-memory state is left unchanged.
