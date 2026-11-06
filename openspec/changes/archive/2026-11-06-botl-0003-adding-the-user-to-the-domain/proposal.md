# Proposal

## Why

Milestone **F1-C01** (Delivery 1, step 3) introduces the **user** as an independent domain object (section 3.1). The user must keep the data later Delivery 1 features need: validation, registration, persistence, deletion and modification. Section 3.2 requires that user representation stays separate from application start-up and from storage.

## What Changes

- Add a user domain object with first name, surname(s), DNI, email address, password, telephone number and card number. All attributes are encapsulated, so state is reachable only through the object's operations.
- The DNI is fixed at creation and cannot be changed afterwards. The other attributes stay modifiable through accessors, ready for the later modification milestone.
- The user object has no persistence or file I/O responsibility. The entry point keeps no file-reading or file-writing logic.
- The milestone's mandated Git message is `[F1-C01] Add user model`.

### In scope
- The user model and its encapsulation, DNI immutability, and the separation-of-responsibilities constraints that apply to the user and the entry point (sections 3.1 and 3.2).

### Out of scope (deferred)
- Attribute validation rules (milestone F1-C02, "valid users").
- The user store and its file persistence. Section 3.2 requires a separate class to store and retrieve users. It is introduced with registration and persistence, the first behaviors that need it.
- Commands of any kind.

## Capabilities

### New Capabilities
- `user-management`: the user concept and, in later milestones, the operations that manage users.

### Modified Capabilities
- None. `project-initialization` and `executable-application` are unchanged.

## Impact

- New domain class in package `es.upm.park.model` and its unit tests.
- No changes to `pom.xml` or the entry point.
