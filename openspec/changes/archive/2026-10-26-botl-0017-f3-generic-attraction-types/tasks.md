# Tasks

## 1. Typed model

- [x] 1.1 Add `AttractionAccess`, `IndividualAccess`, `GroupAccess` and make the ticket classes implement them; verify `mvn compile`
- [x] 1.2 Make `Attraction<T extends AttractionAccess>` abstract with typed `enter(T)`, `admit` and `exit`; add the four subclasses and `AttractionType` factory; verify unit tests: GENERAL admits both kinds, INDIVIDUAL rejects group tickets, GROUP rejects individual tickets, SPECIAL admits individual tickets only, atomicity preserved

## 2. Controller, commands and persistence

- [x] 2.1 Support the typed `ATTRACTION CREATE` syntax (default GENERAL), show the type in STATUS and update HELP descriptions; verify command tests for each type and unknown types
- [x] 2.2 Persist the type (`Id;Nombre;Capacidad;Tipo`, legacy lines as GENERAL); verify a restart test and a legacy-line test, then run `mvn test`

## 3. Model documentation

- [x] 3.1 Update `uml/model.puml` with the interfaces, generic hierarchy and enum; verify `plantuml -checkonly` and the model/code cross-check pass
