# Proposal

## Why

Milestone F1-C04 (Delivery 1, step 6) requires users to survive between runs. Today users created with `USER CREATE` live only in memory and are lost when the application ends.

## What Changes

- Store users in a text file, one user per line, attributes separated by `;` in the order FirstName;Surnames;DNI;Email;Password;Phone;Card (source section 7).
- Load previously stored users when the application starts; a missing file means no users yet.
- Save the file whenever a user is created, so the stored users and the file stay consistent.
- Keep file reading/writing in a dedicated class, separate from the user class and the main class; no databases, persistence frameworks or external libraries.
- Ignore `users.txt` (runtime data) in Git.

### In scope
- Source section 7 (persistence) applied to the existing user-creation flow.

### Out of scope
- Deleting or modifying users (F1-C05, F1-C06), file-based command input (F1-C07), persistence of other entities (Delivery 2).

## Capabilities

### New Capabilities
- `user-persistence`: the user text file format, loading at start-up and saving after changes.

### Modified Capabilities
<!-- None: user-management already forbids persistence in the user class and entry point; this change complies without changing that requirement. -->

## Impact

- New `es.upm.park.persistence` package; `UserRepository` loads from and saves to it; `Main` wires the file location (default `users.txt`, overridable by the `park.users.file` system property for tests).
- `.gitignore` gains `users.txt`.
