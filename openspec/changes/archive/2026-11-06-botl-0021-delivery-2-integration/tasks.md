# Tasks

## 1. Object relationships

- [x] 1.1 Change `Group` to hold `User` members and adapt `GroupService`, `TicketService`, `GroupFileStore`, `GroupRepository` and `ParkController.start`; adapt existing tests and verify the full suite passes, plus new tests: member is the same object after `USER UPDATE` of the email, and after restart the member is the object loaded by the user store
- [x] 1.2 Update `uml/model.puml` (`members` association) and remove the identifier exception from `UmlModelTest`; verify the test passes and PlantUML `-checkonly` succeeds

## 2. End-to-end verification and docs

- [x] 2.1 Add a `MainTest` running a Delivery 2 command file with and without `--fly` from the same empty state, verifying identical outputs, expected results, no leftover temporary data, and complete `HELP`
- [x] 2.2 Update `README.md` with Delivery 2 commands and `--fly`, and verify the documented commands match `HELP`

## 3. Integration

- [x] 3.1 Run `mvn clean package` and verify all tests pass; run the JAR in an empty directory with a Delivery 2 file normally and with `--fly`, and the Delivery 1 example; confirm no regex/serialization/external libraries in `src/main`
- [x] 3.2 Run `openspec validate botl-0021-delivery-2-integration --strict` and verify the change is valid
