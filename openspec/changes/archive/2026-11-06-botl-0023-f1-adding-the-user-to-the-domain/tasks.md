# Tasks

## 1. User model

- [x] 1.1 Add `User` with the seven encapsulated attributes and a final DNI without setter, and verify with `UserTest` the read-back of every attribute, that changing the other attributes keeps the DNI, and (by reflection) that the class has no public fields and no DNI setter

## 2. User store

- [x] 2.1 Add `UserRegistry` (save, find by email/DNI, remove, all, `UserDirectory` adapter), and verify with `UserRegistryTest`
- [x] 2.2 Wire the registry in `AppConfiguration` as the park controller's user directory, and verify with a test that a stored user can buy a ticket through the configured command processor

## 3. Documentation

- [x] 3.1 Update `uml/model.puml` and `README.md`, and verify with the PlantUML syntax check and `mvn clean package`
