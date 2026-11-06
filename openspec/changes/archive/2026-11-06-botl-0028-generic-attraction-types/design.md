# Design

## Context

See proposal.md – Why. `Attraction` is a concrete class (id, name, capacity, occupants); `AttractionService.enter(attractionId, Ticket, today)` checks validity/presence/capacity over `Ticket.holders()`. Tickets: abstract `Ticket`, `IndividualTicket`, `GroupTicket`. Passes: `PassRepository.findByTicket`.

## Goals / Non-Goals

**Goals:**
- Type restrictions enforced by generic bounds, with the bound used in the attraction's operations.

**Non-Goals:**
- SpeedPass entry and pass consumption (F3-C08).

## Decisions

- **Capability interfaces (business.model)**: `Admission` (`getId()`, `isValidOn(LocalDate)`, `holders()`) implemented by `Ticket`; `IndividualAccess extends Admission` (`getUser()`) implemented by `IndividualTicket`; `GroupAccess extends Admission` (`getComposition()`) implemented by `GroupTicket`.
- **`Attraction<T extends Admission>`** (abstract) keeps id/name/capacity/occupants and adds: `Class<T> accepted` (runtime counterpart of the bound), `Optional<T> admissible(Admission)`, `List<User> holdersOf(T)` and `Optional<String> missingRequirement(T, PassLookup)`, plus abstract `getType()`.
  - `GeneralAttraction extends Attraction<Admission>`.
  - `IndividualAttraction extends Attraction<IndividualAccess>`: `holdersOf` = `List.of(t.getUser())`.
  - `GroupAttraction extends Attraction<GroupAccess>`: `holdersOf` = users of `getComposition()`.
  - `SpecialAttraction extends IndividualAttraction`: same bound, overrides `missingRequirement` to require an unconsumed `SpecialPass` among `passes.passesOf(ticket)`.
- **`AttractionType`** enum with `create(id, name, capacity)` factory and strict `fromName`.
- **`PassLookup`** (functional interface in model): `passesOf(IndividualAccess)`; `ParkController` supplies `PassService::passesOf`.
- **`AttractionService.enter(attractionId, Ticket, today, PassLookup)`**: generic helper `<T extends Admission> List<User> admit(Attraction<T>, Ticket, …)`: `admissible` (else `attraction A02 (INDIVIDUAL) does not accept ticket TG01`), validity, `missingRequirement`, presence, capacity, then enter — all checks before any change.
- **Variable field count**: `CommandProcessor.Command` gets `minFields`/`maxFields`; `ATTRACTION CREATE` takes 3–4 fields (error `ATTRACTION CREATE expects 3 to 4 fields`); others unchanged.
- **Persistence**: `id;name;capacity;TYPE`; 3-field lines → GENERAL; unknown types skipped.
- **UML/test**: interfaces, realizations (`<|..`) and the attraction hierarchy added; `UmlModelTest` checks interfaces and realization edges and no longer assumes a fixed number of inheritance edges.
