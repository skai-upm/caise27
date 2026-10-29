# Tasks

## 1. Model

- [x] 1.1 Add `AccessTicket`, `IndividualAccess`, `GroupAccess`, `PassLookup`, `AttractionType`; make tickets implement the capabilities; turn `Attraction` into `Attraction<T extends AccessTicket>` with the four subclasses and the entry template method; verify with unit tests that each type accepts/rejects the right ticket kinds and that the generic parameter of each subclass matches its capability (reflection)

## 2. Operations, commands and persistence

- [x] 2.1 Update `ParkController` (typed creation, generic entry helper, special-pass lookup) and `CommandProcessor` (3/4 arguments, TYPE in STATUS), catalog entry; verify with processor tests for the four source examples, legacy syntax, unknown type, and every access scenario of the spec
- [x] 2.2 Persist the type (`id;name;capacity;type`, legacy lines as GENERAL); verify with a restart test
- [x] 2.3 Update existing tests for the abstract `Attraction` and `uml/model.puml`; run `mvn clean package` and verify `BUILD SUCCESS` and a JAR run creating typed attractions
