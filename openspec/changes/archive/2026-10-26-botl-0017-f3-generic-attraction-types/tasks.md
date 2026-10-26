# Tasks

## 1. Capabilities and generic attractions

- [x] 1.1 Add `Accessible`, `IndividualAccess`, `GroupAccess` and implement them in `Ticket`, `IndividualTicket`, `GroupTicket`; verify `mvn compile`
- [x] 1.2 Make `Attraction<T extends Accessible>` abstract with typed `enter`, `admit`/`enterWith`, and add `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and the `AttractionType` enum/factory; verify with unit tests for each type's admission rules

## 2. Controller, persistence and commands

- [x] 2.1 Update `AttractionController` (typed creation, enterWith), `AttractionRepository` (`;type`, legacy lines as GENERAL), `CommandProcessor` (optional type parameter, `Type:` in status) and the catalog; verify with unit and command tests for every spec scenario
- [x] 2.2 Run `mvn test` and the regression script; verify all tests pass and the output only differs by the new `Type:` status line and HELP

## 3. Model

- [x] 3.1 Update `uml/model.puml` with the interfaces, generic attraction hierarchy and realizations; verify with `plantuml -checkonly`
