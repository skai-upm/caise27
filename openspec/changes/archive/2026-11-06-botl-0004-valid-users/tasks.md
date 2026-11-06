# Tasks

## 1. Field validation rules

- [x] 1.1 Implement `UserValidator.isValidDni` (8 ASCII digits + uppercase check letter via modulo-23 table, null-safe) and verify with unit tests for `12345678Z` (valid), `12345678A`, `1234567Z`, `123456789Z`, `1234567AZ`, `12345678`, null (invalid)
- [x] 1.2 Implement `UserValidator.isValidEmail` with `split("@", -1)` and `lastIndexOf('.')` (no regex) and verify with unit tests for `ana@correo.com` (valid) and `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es`, `a@b@c.com`, null (invalid)
- [x] 1.3 Implement `UserValidator.isValidPassword` with a char loop (no regex/libraries) and verify with unit tests for `Clave1!` (valid) and `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12`, null (invalid)
- [x] 1.4 Implement `UserValidator.isValidCardNumber` (exactly 16 ASCII digits) and verify with unit tests for `1234567890123456` (valid) and `1234 5678 9012 3456`, `123456789012345`, `12345678901234567`, `123456789012345A`, null (invalid)

## 2. Whole-user validation integrated with the model

- [x] 2.1 Implement `UserValidator.isValid(User)` and `User.isValid()` combining the four rules (telephone ignored) and verify with unit tests: fully valid user accepted, one invalid field rejected with attributes unchanged, null field rejected, arbitrary telephone kept unchanged

## 3. Integration

- [x] 3.1 Run `mvn -q test` and `mvn -q package` and verify all tests (including existing `MainTest` and `UserTest`) pass and `target/amusement-park.jar` is produced
- [x] 3.2 Run `openspec validate botl-0004-valid-users --strict` and verify the change is valid
