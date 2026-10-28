# Tasks

## 1. User model

- [x] 1.1 Create `es.upm.amusementpark.model.User` with private fields for first name, surname(s), DNI (final), email, password, telephone number and card number, an all-attributes constructor, getters for all and setters for all but the DNI; verify with unit tests that values round-trip, that setters change only their attribute, and (via reflection) that no field is public and no DNI setter exists
- [x] 1.2 Run `mvn package` and verify `BUILD SUCCESS` with all tests passing (existing execution-mode tests included)

## 2. Milestone commit

- [x] 2.1 Commit with the exact message `[F1-C01] Add user model`; verify with `git log -1 --format=%s`
