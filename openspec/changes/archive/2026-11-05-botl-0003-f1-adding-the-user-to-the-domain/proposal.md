# Proposal

## Why

Milestone F1-C01 (Delivery 1, step 3) introduces the user concept. Later Delivery 1 features (validation, registration, persistence, deletion, modification) all operate on users, so the system first needs a user to exist as an independent, properly encapsulated object. Today the codebase only has the `App` entry point.

## What Changes

- Add a user domain object holding the seven attributes from source §3.1: first name, surname(s), DNI, email address, password, telephone number and card number.
- Encapsulate all attributes: they are reachable only through accessor methods, never as public fields.
- Make the DNI immutable after creation (no mutator); the other attributes can be changed later through mutators, as user modification (a later milestone) needs.
- Keep the user class free of persistence and I/O, and separate from the application entry point (source §3.2).

In scope: the user model and its encapsulation and DNI immutability.

Out of scope: attribute validation rules (milestone "valid users"), the user store and registration (later milestones), file persistence, and commands.

## Capabilities

### New Capabilities
- `user-model`: The user as a domain object — its attributes, encapsulation and the immutability of its identifying DNI.

### Modified Capabilities
- None. `project-initialization` and `executable-application` are unchanged.

## Impact

- New class `src/main/java/es/upm/parque/model/User.java` and its unit tests.
- No change to `App`, the build or the JAR packaging.
