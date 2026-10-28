# Tasks

## 1. User model

- [x] 1.1 Add first name, surnames, telephone and card to `User` with private fields, accessors, setters (none for DNI) and a full constructor; verify with unit tests (values, all fields private, DNI final without setter)

## 2. Persistence and documentation

- [x] 2.1 Store and read the full user line in `TextUserRepository` (2/3-field lines still load); verify with persistence round-trip tests
- [x] 2.2 Add the attributes to `uml/model.puml`; verify `UmlModelTest` and the PlantUML syntax check

## 3. Delivery

- [x] 3.1 Run `mvn clean package` (BUILD SUCCESS)
- [x] 3.2 Commit with the exact message `[F1-C01] Add user model`; verify with `git log -1 --format=%s`
