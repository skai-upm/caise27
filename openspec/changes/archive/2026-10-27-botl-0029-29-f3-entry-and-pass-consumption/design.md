# Design

## Context

`Attraction<T extends AttractionAccess>.admit(T, today)` performs atomic entry; passes live in `PassRepository` (persistence layer), which the model must not use directly.

## Decisions

- **`PassLookup`** (business.model functional interface) `List<Pass> passesOf(IndividualTicket)`; the controller supplies `passes::findByTicket`, keeping the model independent of persistence.
- **`AttractionAccess.individualTickets()`**: individual ticket → itself; group ticket → its composition. Lets pass rules treat both uniformly.
- **`Attraction.admit(T access, LocalDate today, boolean speed, PassLookup passes)`** returns `EntryResult(users, consumedPasses)`: validity → `passesToConsume(access, speed, passes)` → not-inside → capacity → consume + add occupants. Nothing is mutated before all checks pass.
- **`passesToConsume`** default: no passes unless `speed`; with `speed`, first unconsumed `SpeedPass` per individual ticket or `OperationNotAllowedException`. **`SpecialAttraction`** overrides with its `IndividualAccess` parameter: rejects `speed`, returns the ticket's unconsumed `SpecialPass` or fails — the special rule is the attraction's own behaviour, not a different ticket type.
- `AttractionController.configure(attractions, tickets, passes, park)`; `enter(attraction, ticket, speed)`.
- UML: `PassLookup` and `EntryResult` are added to `uml/model.puml` to keep the diagram–code consistency test passing.
- Presentation: `ATTRACTION ENTER` accepts 2 or 3 arguments, the third must be `SPEED`; consumed passes are appended to the output.
