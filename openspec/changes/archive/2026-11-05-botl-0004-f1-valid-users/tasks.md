# Tasks

## 1. Field validators

- [x] 1.1 Implement `UserValidator.isValidDni` (8 ASCII digits + uppercase check letter from `TRWAGMYFPDXBNJZSQVHLCKE`, null-safe) and verify with tests for `12345678Z` valid, `12345678A` and wrong-shape values invalid
- [x] 1.2 Implement `UserValidator.isValidEmail` with `split`/String operations only and verify tests: `ana@correo.com` valid; `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` invalid
- [x] 1.3 Implement `UserValidator.isValidPassword` without regex and verify tests: `Clave1!` valid; `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` invalid
- [x] 1.4 Implement `UserValidator.isValidCardNumber` and verify tests: `1234567890123456` valid; `1234 5678 9012 3456`, 15 digits, 17 digits, digits with a letter invalid

## 2. Whole-user check

- [x] 2.1 Implement `UserValidator.isValid(User)` combining the four checks (telephone ignored) and verify tests: fully valid user valid, one invalid card makes it invalid, user attributes unchanged, unusual telephone does not affect validity
- [x] 2.2 Verify no regular expressions are used in the validator (grep for `matches(`, `Pattern`, `replaceAll(`) and run `mvn package` plus `java -jar target/amusement-park.jar < /dev/null` for regression
- [x] 2.3 Resolve Sonar finding java:S3516 on `isValidPassword` (flag-accumulator pattern reported as always returning the same value) by refactoring into per-category checks; verify `mvn package` tests still pass and the SDDX apply quality gate is OK
