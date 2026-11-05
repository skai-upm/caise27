# Tasks

## 1. Package moves

- [x] 1.1 Move `cli` and `*Commands` to `presentation`, the remaining `park` classes to `business` (with their tests), and update packages and imports; verify `mvn -q test` passes unchanged

## 2. Exception design

- [x] 2.1 Add `InvalidCommandException`, make handlers throw instead of printing, and centralize handling in `CommandProcessor`; verify existing message assertions plus new malformed and refused scenario tests pass

## 3. Layering guard and documentation

- [x] 3.1 Add `LayeringTest` (no default package, dependency rules); verify it passes
- [x] 3.2 Update `uml/model.puml` packages and add `InvalidCommandException`; verify `UmlModelTest` passes

## 4. Verification

- [x] 4.1 Run `mvn -q package` and the JAR with a phase-2 file; verify all tests pass and output is unchanged
