# Tasks

## 1. Repository interfaces

- [x] 1.1 Add `UserRepository`, `GroupRepository`, `TicketRepository`, `GroupTicketRepository`, `AttractionRepository` and make the `*FileStorage` classes implement them; change controller fields/parameters to the interfaces; verify `mvn compile`

## 2. Singleton controllers

- [x] 2.1 Give the five controllers a private constructor, `initialize(...)` and `getInstance()`; wire them in `App.createProcessor` in dependency order and inject repositories; update tests to use `initialize`; verify `mvn clean test` passes
- [x] 2.2 Add `ComponentLifecycleTest`: same instance from `getInstance()`, `ParkController.users()` and the command layer; error before initialisation; an in-memory `UserRepository` works with `UserController`; `--fly` and normal runs give identical responses; `grep` shows `new *Controller`/`new *FileStorage` only in `App`

## 3. UML and verification

- [x] 3.1 Update `uml/model.puml` (repository interfaces, realisations, controller associations to interfaces, singleton `instance` self-references); verify `plantuml -checkonly`, `mvn package`, and the previous regression script gives identical output
