# Proposal

## Why

Milestone F1-C01 introduces the user as defined by the assignment: an independent, encapsulated object with first name, surname(s), DNI, e-mail, password, telephone number and card number, whose DNI cannot change after creation, kept separate from the classes that start the application and that store/retrieve users. In this batch earlier changes already created a minimal user (DNI, e-mail and — since F3-C05 — a BCrypt-protected password) and separate main, controller and repository classes; the remaining personal attributes are still missing.

## What Changes

- Complete the user model with first name, surname(s), telephone number and card number, all private with accessors; DNI stays final (no setter); the other attributes can be modified through encapsulated setters for later management operations. The password keeps being held only as its protected value (F3-C05 requirement, which supersedes plain storage).
- Persist the new attributes in the user file (`dni;email;passwordHash;firstName;surnames;phone;card`), still reading the shorter formats of earlier versions.
- Update the UML model.
- Commit with the exact message `[F1-C01] Add user model`.

## Capabilities

### New Capabilities
- `user-management`: The user concept, its attributes and encapsulation rules (later extended with validation, registration, deletion and modification).

### Modified Capabilities
- `park-persistence`: users are stored with all their attributes.

## Impact

- `User`, `TextUserRepository`, UML, tests.
- **Flagged ambiguities / ordering conflicts:**
  - This milestone is applied after Delivery 3 in this batch: the password attribute is therefore the BCrypt-protected value required by F3-C05, not plain text.
  - Validation of the attributes is F1-C02; creation through `USER CREATE` is F1-C03.
  - Users created by earlier changes without personal data load with empty values for the new attributes.
