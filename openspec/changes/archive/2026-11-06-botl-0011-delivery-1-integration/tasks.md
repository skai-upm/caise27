# Tasks

## 1. Controlled execution

- [x] 1.1 Extract `es.upm.park.command.CommandRunner` (shared loop with EXIT check, file and stdin sources, controlled unreadable-source message) from `Main`, leaving `Main` with wiring only, and verify with unit tests for `CommandRunner` and the existing `MainTest`
- [x] 1.2 Handle `PersistenceException` at start-up in `Main` (message, no command processed, file untouched) and verify with a `MainTest` using a directory as the user file
- [x] 1.3 Add the `RuntimeException` safety net in `CommandProcessor.process` and verify with a unit test using a service that throws unexpectedly

## 2. End-to-end verification

- [x] 2.1 Add a `MainTest` running the section 16 sequence in file and stdin mode and verify identical output and final user file (only Ana, phone `699999999`, email `ana.nueva@correo.com`)
- [x] 2.2 Add a `MainTest` with a mixed file of incorrect and valid lines and verify an error per incorrect line, valid effects applied, normal end
- [x] 2.3 Document build and run usage in `README.md` and verify the documented commands work as written

## 3. Integration

- [x] 3.1 Run `mvn clean package` from scratch and verify all tests pass; run the JAR in an empty directory in file and interactive modes with the section 16 sequence; confirm `grep` finds no regex API in `src/main` and `pom.xml` declares only test-scoped dependencies
- [x] 3.2 Run `openspec validate botl-0011-delivery-1-integration --strict` and verify the change is valid
