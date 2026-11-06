# Tasks

## 1. User model

- [x] 1.1 Create `es.upm.park.model.User` with private fields for first name, surname(s), DNI, email, password, telephone and card number, an all-attributes constructor and getters; verify with a unit test that all values round-trip
- [x] 1.2 Add setters for all attributes except DNI (DNI field `final`, no setter); verify with unit tests that a setter changes only its attribute, two users are independent, and reflection shows no DNI setter and a final DNI field
- [x] 1.3 Verify the user class performs no file I/O (no `java.io`/`java.nio.file` usage) and `Main` remains free of file logic

## 2. Regression

- [x] 2.1 Run `mvn package` and verify BUILD SUCCESS, all tests pass and `java -jar target/amusement-park.jar` still starts
