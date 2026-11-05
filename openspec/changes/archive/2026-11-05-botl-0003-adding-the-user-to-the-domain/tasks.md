# Tasks

## 1. User model

- [x] 1.1 Create `es.upm.amusementpark.model.User` with private fields for first name, surnames, DNI (final), email, password, telephone and card number, a constructor and getters; verify a unit test checks every attribute is returned unchanged
- [x] 1.2 Add setters for every attribute except DNI; verify unit tests change each mutable attribute and the DNI stays the same
- [x] 1.3 Add a reflection-based unit test asserting no public fields and no DNI setter exist; verify it passes

## 2. Regression

- [x] 2.1 Run `mvn clean package` and verify all tests pass and `target/amusement-park.jar` is still produced
