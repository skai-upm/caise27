# Design

## Context

`Attraction` is a concrete class; `AttractionService.enter` checks date, already-inside and capacity for any `Ticket`. Tickets: abstract `Ticket` with `IndividualTicket` and `GroupTicket`. Passes are found in `PassRepository`.

## Goals / Non-Goals

**Goals:**
- Capability interfaces + bounded generics that actually restrict accepted tickets; four attraction types; persisted type.

**Non-Goals:**
- SpeedPass entry option and Special Pass consumption (F3-C08).

## Decisions

- **Capabilities (`business.model`):** `AccessTicket` (identifier, date, `getHolders()`), `IndividualAccess extends AccessTicket` (`getUser()`), `GroupAccess extends AccessTicket` (`getComposition()`). `Ticket implements AccessTicket`, `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- **`abstract class Attraction<T extends AccessTicket>`** keeps identity, capacity and occupants and receives `Class<T> accessType`. `T accept(Ticket, PassLookup)` rejects tickets that are not `T` and calls the typed hook `checkAdmission(T, PassLookup)`; `admit(T)` performs the already-inside and capacity checks and lets the holders in. The type parameter therefore determines which tickets the typed operations take.
- **Subclasses:** `GeneralAttraction extends Attraction<AccessTicket>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` overriding `checkAdmission` to require an unconsumed `SpecialPass` from the injected `PassLookup` (a functional interface the service implements with `PassRepository::findByTicket`). No separate ticket hierarchy for special attractions.
- **`AttractionType` enum** (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) with `create(id, name, capacity)` and `fromName` (exact uppercase, flagged); each attraction reports its type.
- **Service:** `AttractionService.enter` keeps the date check and delegates to a generic helper `<T extends AccessTicket> admit(Attraction<T>, Ticket)`; `PassRepository` injected for the lookup. `createAttraction(id, name, capacity, type)`.
- **Persistence:** `attractions.txt` lines `id;name;capacity;type`; 3-field lines load as GENERAL.
- **Special Pass at SPECIAL entry (flagged):** this milestone checks that an unconsumed Special Pass exists; marking it consumed is F3-C08's "automatic Special Pass consumption".
- **Mandatory commit:** `[F3-C07] Add generic attraction types` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Runtime type check is still needed because tickets are looked up by identifier] → `Class<T>` token kept by each attraction; the cast is done once in the generic base.
