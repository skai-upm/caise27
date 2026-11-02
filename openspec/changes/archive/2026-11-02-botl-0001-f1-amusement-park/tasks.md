# Tasks

## 1. F0-C01 Project initialization

- [x] 1.1 Generate the project with `maven-archetype-quickstart` 1.5 (groupId `es.upm.etsisi`, artifactId `amusement-park`, package `es.upm.etsisi.amusementpark`), move it to the repository root and verify `pom.xml`, `src/main/java` and `src/test/java` exist
- [x] 1.2 Set `maven.compiler.release` to 21 in `pom.xml` and verify `mvn test` succeeds

## 2. F0-C02 Executable application

- [x] 2.1 Set `<finalName>nombre-aplicacion</finalName>` and the `maven-jar-plugin` manifest `mainClass` to `es.upm.etsisi.amusementpark.App`; verify `mvn package` produces `target/nombre-aplicacion.jar` and `java -jar target/nombre-aplicacion.jar` starts and reads standard input

## 3. F1-C01 User model

- [x] 3.1 Implement `user.User` with the seven encapsulated attributes, a final DNI without setter and a copy operation; verify with `UserTest` (attributes readable, no DNI mutator exists)
- [x] 3.2 Implement `user.UserField` enum with exactly `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; verify with a test asserting the value set

## 4. F1-C02 User data validation

- [x] 4.1 Implement regex-free `user.UserValidator` for DNI, email, password and card number; verify with `UserValidatorTest` covering every valid and invalid example in specs/user-data-validation

## 5. F1-C03 User creation

- [x] 5.1 Implement `user.UserManager.create` with validation and DNI/email uniqueness before any state change; verify with `UserManagerTest` (valid create, invalid data, duplicate DNI, duplicate email)
- [x] 5.2 Implement `command.CommandProcessor` parsing with `split(";", -1)` and `USER CREATE` handling, including wrong element counts and unknown commands; verify with `CommandProcessorTest` (`USER CREATED`, errors, no termination)

## 6. F1-C04 User persistence

- [x] 6.1 Implement `persistence.UserStorage` load/save in the `;`-separated one-line-per-user format using `java.nio.file`; verify with `UserStorageTest` (exact line format, round trip, missing file, malformed line skipped)
- [x] 6.2 Wire `UserManager` to load at construction and save before committing each mutation, keeping memory unchanged on write failure; verify restart and write-failure scenarios in `UserManagerTest`

## 7. F1-C05 User deletion

- [x] 7.1 Implement `UserManager.delete` and `USER DELETE` handling (locate by email, verify password, delete, persist); verify correct-credentials, wrong-password and unknown-email tests in `UserManagerTest` and `CommandProcessorTest`

## 8. F1-C06 User update

- [x] 8.1 Implement `UserManager.update` and `USER UPDATE` handling for all enum fields with authentication, creation-rule validation, email uniqueness against other users and persistence; verify tests for NAME/EMAIL/PASSWORD/PHONE/CARD success, DNI rejected, unknown field, duplicate email, invalid password and invalid card

## 9. F1-C07 Command file input

- [x] 9.1 Implement `command.CommandRunner` reading lines from any `BufferedReader`, skipping blank lines, sharing `CommandProcessor`, plus controlled handling of a missing/unreadable file; wire `App` for interactive and file mode; verify with `CommandRunnerTest` (ordered processing, blank lines, equivalent interactive/file results, missing file message)

## 10. F1-C08 Help and exit

- [x] 10.1 Implement `HELP` (syntax listing) and `EXIT` (stop processing, no later lines executed) in `CommandProcessor`/`CommandRunner`; verify with tests for help content, interactive exit and commands after `EXIT` in a file

## 11. F1-C09 Delivery 1 integration

- [x] 11.1 Run `mvn clean package` and execute the packaged JAR in file mode with the source's basic execution example, then again in interactive mode via piped stdin; verify the outputs and the resulting `users.txt` match specs/delivery-one-integration
- [x] 11.2 Verify Delivery 1 restrictions: no runtime dependencies in `pom.xml` (JUnit test scope only) and no `regex`/`Pattern`/`matches(`/`replaceAll(`/`javax.crypto` usage in `src/main/java`
