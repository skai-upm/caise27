# Tasks

## 1. Storage and service

- [x] 1.1 Create `es.upm.park.repository.UserRepository` (in-memory add/findByDni/existsByEmail/findAll) and verify with unit tests for add, lookup and unmodifiable listing
- [x] 1.2 Create `es.upm.park.service.UserException` and `UserService.create(...)` validating DNI, email, password, card (in that order) and DNI/email uniqueness, and verify with unit tests: valid user stored; each invalid field rejected with its own message and no user added; duplicate DNI and duplicate email rejected with stored users unchanged

## 2. Command interpretation

- [x] 2.1 Create `es.upm.park.command.CommandProcessor.process(String)` splitting on `;`, dispatching `USER CREATE`, reporting wrong field counts and unknown commands as `Error:` messages without throwing, and verify with unit tests for success, wrong field count, unknown command and blank line

## 3. Interactive application

- [x] 3.1 Update `Main` to print the banner, then read standard input line by line through `CommandProcessor` and print each result until end of input; update `MainTest` to supply standard input and verify banner-only output on empty input and two ordered results for two create commands

## 4. Integration

- [x] 4.1 Run `mvn package` and verify all tests pass, then run `printf 'USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nFOO\nUSER CREATE;Luis;Perez;12345678Z;l@correo.com;Clave1!;6;1111222233334444\n' | java -jar target/amusement-park.jar` and verify one creation, one unknown-command error and one duplicate-DNI error
- [x] 4.2 Run `openspec validate botl-0005-user-registration --strict` and verify the change is valid
