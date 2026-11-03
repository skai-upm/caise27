# Tasks

## 1. Capabilities and generic attractions

- [x] 1.1 Add `AccessTicket`, `IndividualAccess`, `GroupAccess` and implement them in `ParkTicket`/`Ticket`/`GroupTicket`; make `Attraction<T extends AccessTicket>` abstract with typed `admit` and runtime-checked `enter`, plus the four subclasses and `AttractionType`; verify `AttractionTypesTest` (each type admits/rejects the right kinds, typed `admit` used with compatible tickets) and existing attraction tests

## 2. Commands and persistence

- [x] 2.1 Support `ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` in `AttractionController`/`CommandProcessor`/`CommandCatalog` and store the type (`Id;Nombre;Capacidad;Tipo`, old lines → GENERAL); verify command tests (default GENERAL, explicit GROUP, unknown type, GROUP rejects T001, INDIVIDUAL rejects TG01, SPECIAL admits only individual) and storage round trip with an old 3-field line

## 3. UML and verification

- [x] 3.1 Update `uml/model.puml` (interfaces, realisations, generic class and bindings); verify `plantuml -checkonly`, `mvn clean test`, and the regression script (only HELP and the attraction file's type column change)
