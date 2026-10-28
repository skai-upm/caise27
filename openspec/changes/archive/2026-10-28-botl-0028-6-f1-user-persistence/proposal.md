# Proposal

## Why

Milestone F1-C04 requires users to be preserved between runs in a text file using the format specified by the assignment — one user per line, attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card` — loaded at start-up by a class separate from the user and main classes, without databases, frameworks or persistence libraries. Users are already persisted in this batch (since F2-C08/F1-C01), but with a different attribute order.

## What Changes

- Write the user file in the assignment's attribute order `FirstName;Surnames;DNI;Email;Password;Phone;Card`, where the Password field holds the BCrypt-protected value required by F3-C05 (never the original password).
- Keep reading the user lines written by earlier versions of this project (`DNI;Email`, `DNI;Email;Hash`, and the previous seven-field order).
- Verify that users created with `USER CREATE` survive a restart and that reading/writing stays in the persistence layer (not in the user or main class).
- Commit with the exact message `[F1-C04] Add user persistence`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `park-persistence`: the user file follows the assignment's attribute order.

## Impact

- `TextUserRepository` format, tests.
- **Flagged ambiguities:**
  - The assignment's example stores plain passwords (`Clave1!`); Delivery 3 (F3-C05), already applied in this batch, forbids storing the original password, so the Password position holds the BCrypt value.
  - Earlier formats are recognised by the position of the `@` (field 2 for the previous order, field 4 for the assignment order).
