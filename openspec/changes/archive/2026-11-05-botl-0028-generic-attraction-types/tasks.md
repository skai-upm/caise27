# Tasks

## 1. Model

- [x] 1.1 Add access interfaces implemented by tickets, generic `Attraction<T>` with the four subclasses and `AttractionType`; verify unit tests that each type admits only its bound and capacity/atomicity still hold

## 2. Commands and storage

- [x] 2.1 Support `ATTRACTION CREATE` with optional type (default GENERAL), show type in STATUS, store/load type (3-field lines as GENERAL), and route entry through the typed gate; verify command and persistence tests

## 3. Integration

- [x] 3.1 Update UML and run `mvn clean package` plus the JAR with the four attraction types and accepted/rejected entries
