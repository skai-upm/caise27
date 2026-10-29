# Tasks

## 1. Model

- [x] 1.1 Create minimal `User` (private email, getter, equals/hashCode by email) and a user directory in `ParkController` (`registerUser`, `findUser`, cleared by `reset()`); verify with unit tests for lookup of registered and unknown emails
- [x] 1.2 Create `Group` (private identifier, name, `LinkedHashSet<User>` members, add/remove returning success, unmodifiable members view); verify with unit tests that duplicates are rejected and removal of a non-member fails

## 2. Group operations

- [x] 2.1 Add group storage and `createGroup`, `addUserToGroup`, `removeUserFromGroup`, `deleteGroup` to `ParkController`, with errors for duplicate/blank identifier, blank name, unknown group, unknown user, duplicate member, non-member; verify with unit tests for each success and rejection case
- [x] 2.2 Add `hasActiveGroupTicket(Group)` computed from group tickets and the current date (no stored lock flag) and enforce it in add/remove/delete; verify with a unit test that a group without tickets is modifiable and deletable, and by inspection that `Group` has no lock field

## 3. Commands

- [x] 3.1 Dispatch `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE` in `CommandProcessor` with argument-count checks and confirmation messages; verify with processor tests covering the source examples (`GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`) and rejections
- [x] 3.2 Add the four GROUP syntaxes to HELP; verify with a test asserting they appear in the help output
- [x] 3.3 Run `mvn clean package` and verify `BUILD SUCCESS`; run the JAR with `GROUP CREATE;G01;Familia Garcia`, `GROUP ADD;G01;ana@correo.com`, `GROUP DELETE;G01`, `EXIT` and verify the outputs (user not found until USER CREATE exists)
