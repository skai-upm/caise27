# Tasks

## 1. User creation service

- [x] 1.1 Add `UserRepository.existsByDni`, `UserOperationException` and `UserService.createUser` (validate, check DNI/email uniqueness, store) and verify with `UserServiceTest`: success, invalid DNI, invalid password, duplicate DNI and duplicate email all behave as specified with nothing stored on failure

## 2. USER CREATE command

- [x] 2.1 Parse `;`-separated commands in `CommandProcessor`, dispatch `USER CREATE` (exactly 8 elements) to `UserService`, print `USER CREATED` or `ERROR: ...`, and verify with `CommandProcessorTest` the success output, wrong element count, a malformed command followed by `HELP`, and that HELP/EXIT/unknown commands still behave as before
- [x] 2.2 Run `mvn package` and verify with the JAR that `printf 'USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nUSER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n' | java -jar target/amusement-park.jar` prints `USER CREATED` then a duplicate error
