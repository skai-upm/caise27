# Tasks

## 1. User entity

- [x] 1.1 Create `model/User` with private fields for the seven attributes, all-args constructor, getters, setters for every attribute except DNI (final, no setter), and DNI-based `equals`/`hashCode`; verify with `UserTest` (attributes read back, setters update non-DNI fields, no public fields, no `setDni` method, no `java.io` dependency)
- [x] 1.2 Verify the existing build still works with `mvn package` and `MainTest` passing

## 2. Milestone commit

- [x] 2.1 Commit the change with the exact message `[F1-C01] Add user model`; verify with `git log -1 --format=%s`
