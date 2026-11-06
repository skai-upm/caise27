# Tasks

## 1. Repository

- [x] 1.1 Add the `UserStorage` port, `UserRepository` (users.txt, seven fields, tolerant loading) and `StoragePaths.users()`, and verify with `UserRepositoryTest` a round trip, the field mapping of a stored line, and a malformed line skipped with a warning

## 2. Wiring

- [x] 2.1 Make `UserController` save through the injected `UserStorage` after creating a user, add `UserRegistry.restore`, and load users in `App` at startup, and verify with `UserPersistenceTest` (App runs) that users survive a restart, the file has no plain-text password, and a `--fly` run neither reads nor changes normal users

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
