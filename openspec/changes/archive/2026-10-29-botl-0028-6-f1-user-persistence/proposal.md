# Proposal

## Why

Milestone F1-C04 requires users to be preserved between executions in a text file with the format specified in the assignment — one user per line, attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card` — loaded at start-up, with file reading and writing in a class separate from the user class and the main class. Users are already persisted in this batch, but in a different field order (`email;dni;password;…`).

## What Changes

- The users file uses the assignment's order: `FirstName;Surnames;DNI;Email;Password;Phone;Card`, one user per line.
- The password field contains the BCrypt-protected value instead of the plain password shown in the Delivery 1 example (Delivery 3 forbids plain storage and replaces that part of the format).
- Loading at start-up reads this format; files written earlier in this batch (email-first lines, and 2/3-field lines) are still read.
- Separation remains: `TextFileUserRepository` (persistence) does all file I/O; `User` and `App` do none.

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `users`: the persisted user format.

## Impact

- `TextFileUserRepository`, storage tests.

## Ambiguities (flagged)

- Plain-text passwords in the example conflict with Delivery 3's password protection; the protected value is stored in the password position.
- Users known only partially (email and DNI, from older data) are written with empty fields for the unknown attributes.
