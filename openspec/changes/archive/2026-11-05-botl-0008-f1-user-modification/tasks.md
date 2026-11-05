# Tasks

## 1. Model

- [x] 1.1 Add `model.UserField` enum with `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD` and a null-returning `fromName(String)`; verify with a test that all six parse, `DNI`/`AGE`/lowercase return null

## 2. Store and command

- [x] 2.1 Add `UserRepository.update(User)` that persists the current set; verify test that a changed attribute is in the reloaded file
- [x] 2.2 Implement `USER UPDATE;Email;Password;Field;NewValue` in `CommandProcessor` (count, auth, field, validation/uniqueness, set, persist); verify `CommandProcessorTest` cases: each of the six fields updates, wrong password, unknown email, DNI field, unknown field, invalid email/password/card, email of another user, own email accepted, new password used for later auth, wrong element count
- [x] 2.3 Change the not-yet-supported-command test example to `USER PROMOTE;ana@correo.com` and verify `mvn test` passes

## 3. Integration

- [x] 3.1 End-to-end in a temp directory with the JAR: create a user, update CARD, restart and verify the file holds the new card; attempt DNI update (ERROR); exit status 0
