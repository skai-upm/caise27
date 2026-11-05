# Design

## Context

`Attraction` is a concrete class with capacity and occupants; `AttractionService.enter` uses `Ticket.admittedUsers()` for all tickets.

## Goals / Non-Goals

**Goals:** a generic parameter that controls which tickets an attraction handles; extension points for F3-C08 (SpeedPass, Special Pass). **Non-Goals:** pass consumption.

## Decisions

- **Capabilities** (`business.model`): `TicketAccess` (date validity + admitted users, implemented through `Ticket`), `IndividualAccess extends TicketAccess` (single user), `GroupAccess extends TicketAccess` (composition of individual accesses). `IndividualTicket implements IndividualAccess`, `GroupTicket implements GroupAccess`.
- **`abstract class Attraction<T extends TicketAccess>`** keeps the bound's `Class<T>`; `enter(Ticket)` checks `accessType.isInstance`, casts to `T` and calls the typed hooks `checkEntry(T)` and `admit(T)`; only `T` instances can reach the typed operations, so the parameter has effect. Subclasses: `GeneralAttraction extends Attraction<TicketAccess>`, `IndividualAttraction extends Attraction<IndividualAccess>`, `GroupAttraction extends Attraction<GroupAccess>`, `SpecialAttraction extends Attraction<IndividualAccess>` (its own `checkEntry` will add the Special Pass rule in F3-C08).
- **`AttractionType` enum** with a factory `create(id, name, capacity)`; the attraction reports its type.
- **Command registry** gains optional elements (min/max count) so `ATTRACTION CREATE` takes 4 or 5 elements.
- **Persistence**: `id;name;capacity;type`; a 3-field line loads as GENERAL.

## Risks / Trade-offs

- [Raw `Attraction<?>` in repositories/services] → services only use the type-safe `enter(Ticket)` gate.
