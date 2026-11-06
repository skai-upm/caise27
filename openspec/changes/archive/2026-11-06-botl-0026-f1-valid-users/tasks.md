# Tasks

## 1. Validation

- [x] 1.1 Add `UserValidator` with the DNI, email, password and card predicates (no regex), and verify with `UserValidatorTest` every valid and invalid example of the source plus shape edge cases
- [x] 1.2 Add the throwing checks and `validateUser` naming the failing field, and verify with tests that each invalid field is reported and that phone, first name and surnames only need to be non-empty

## 2. Integration

- [x] 2.1 Use `UserValidator.passwordPolicy()` in `AppConfiguration` for `PasswordService`, and verify with `AppConfigurationTest` that `clave1!` is rejected and `Clave1!` is protected

## 3. Documentation

- [x] 3.1 Update `uml/model.puml`, the UML consistency check (validation package) and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
