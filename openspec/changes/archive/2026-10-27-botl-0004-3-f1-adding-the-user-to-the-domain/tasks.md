# Tasks

## 1. User domain model

- [x] 1.1 Create `User` with the seven encapsulated attributes (DNI final, no DNI setter) and verify with `UserTest` that all values are returned and that mutable attributes can be changed while the DNI cannot (no setter, reflection check that the field is final)

## 2. User storage

- [x] 2.1 Create `UserRepository` (in-memory, keyed by email: save, findByEmail, delete, findAll) separate from `User` and `App`, and verify with `UserRepositoryTest` storing, retrieving, unknown-email lookup and deletion
- [x] 2.2 Run `mvn package` and verify the build and all tests pass
