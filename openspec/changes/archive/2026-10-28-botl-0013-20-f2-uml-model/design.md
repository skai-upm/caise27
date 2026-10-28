# Design

## Context

Model package `es.upm.etsisi.amusementpark.model`: `User` (dni, email), `Group` (identifier, name, members), abstract `Ticket` (identifier, date; price derived from individual tickets), `IndividualTicket` (user, price), `GroupTicket` (group, composition), `Attraction` (identifier, name, capacity, occupants). `ParkController` (singleton) holds users, groups, tickets, attractions, current date and price.

## Decisions

- **Associations rendered as attributes-as-relations**: reference fields (`user`, `group`, `members`, `composition`, `occupants`) are drawn as directed relations with role names equal to the field name instead of attribute lines, so directions and multiplicities are explicit.
- **Composition `GroupTicket *--> "1..*" IndividualTicket : composition`**; aggregation for `Group o--> "*" User : members` and `Attraction o--> "0..*" User : occupants` (users exist independently).
- **Controller**: `ParkController` composes `User`, `Group`, `Ticket`, `Attraction` (`"1" *--> "*"`), with `currentDate`/`currentPrice` attributes and a `{static}` `INSTANCE` marker of the singleton.
- **Consistency test** (`UmlModelTest`): lists `src/main/java/.../model/*.java`, loads each class, asserts the diagram declares `class <Name>` (or `abstract class`) and mentions every non-static declared field name; asserts every `class X` declared in the diagram exists in the model package or is `ParkController`. Text matching uses `contains`/`indexOf` (no regex).
- **Syntax check**: PlantUML jar from the local Maven cache with `-checkonly` during verification (not added as a project dependency).
