# Proposal

## Why

Milestone F1-C01 requires the user concept as defined in the assignment: independent user objects keeping first name, surname(s), DNI, email, password, telephone and card number, properly encapsulated, with an unchangeable DNI, and with separate classes for the user, for starting the application, and for storing and retrieving users. In this batch the `User` class so far only holds email, DNI and a password hash (added earlier to support groups, tickets and password protection).

## What Changes

- `User` holds all seven attributes as private fields with accessors; DNI is final (no way to change it after creation); the other attributes have controlled setters for the later management operations.
- The password attribute is kept as its BCrypt-protected value (Delivery 3 forbids storing it in plain text).
- User identity (equality) is based on the immutable DNI instead of the email, so a later email change cannot break collections that contain the user.
- The users file stores every attribute; older files with fewer fields are still read.
- Separation of responsibilities is kept and verified: `User` (domain, no persistence), `App` (start-up, no file reading/writing), `TextFileUserRepository` (storage).

## Capabilities

### New Capabilities
- `users`: the user's data, encapsulation and immutability rules.

### Modified Capabilities
- None.

## Impact

- `User`, `TextFileUserRepository`, `ParkController` (registration keeps working), tests, UML.

## Ambiguities and unmet prerequisites (flagged)

- Validation of DNI, email, password and card (F1-C02) and USER CREATE (F1-C03) come in later proposals of this batch; here users are still created programmatically and the model does not validate values.
- A second, partial constructor (email and DNI only) is kept for historical tickets whose user data is incomplete (embedded email/DNI) and for existing callers; the full constructor carries all attributes.
- Equality by DNI is a design decision; the source only fixes that the DNI is immutable.
