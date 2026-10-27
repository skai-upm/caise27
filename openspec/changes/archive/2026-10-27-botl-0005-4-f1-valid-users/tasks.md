# Tasks

## 1. Field validation rules

- [x] 1.1 Implement `UserValidator.isValidDni` (8 digits + correct mod-23 letter) and verify with `UserValidatorTest` that `12345678Z` passes and `12345678A`, `1234567Z`, `123456789Z`, `12345678` fail
- [x] 1.2 Implement `UserValidator.isValidEmail` with String operations/split (no regex) and verify `ana@correo.com` passes and `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` fail
- [x] 1.3 Implement `UserValidator.isValidPassword` and verify `Clave1!` passes and `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` fail
- [x] 1.4 Implement `UserValidator.isValidCard` and verify `1234567890123456` passes and `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` fail

## 2. Reusable whole-user validation

- [x] 2.1 Implement `UserValidator.validate(dni, email, password, card)` returning the list of error messages and verify valid data yields none and DNI `12345678A` + card `123` yield DNI and card errors; verify no `java.util.regex` usage (`grep`), and run `mvn package` successfully
