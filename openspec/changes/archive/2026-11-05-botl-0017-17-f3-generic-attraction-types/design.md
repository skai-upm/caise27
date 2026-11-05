# Design

## Context

`business.model.Attraction` is concrete (id, name, capacity, present users; `enter(Ticket, today)` uses `Ticket.getHolders()`); `AttractionManager.create(id, name, capacityText)`; `AttractionFileStore` line `Id;Nombre;Capacidad`. `Ticket` is abstract with `IndividualTicket`/`GroupTicket`. See proposal.md for motivation.

## Goals / Non-Goals

**Goals:** type-safe admission via an interface plus bounded generics; backward-compatible command and file.

**Non-Goals:** Special Pass checks and pass consumption at entry (next change); STATUS format changes.

## Decisions

- **`AccessCredential`** (model interface): `getId()`, `getDate()`, `getHolders()` — the capabilities an attraction needs from anything used to enter. `Ticket implements AccessCredential`.
- **`Attraction<T extends AccessCredential>`** (abstract): holds `Class<T> admitted`; `accepts(AccessCredential)` = `admitted.isInstance`; public `enter(AccessCredential, today)` rejects other kinds, then calls the typed `admit(T credential, today)` which applies date, already-present, `checkAdditional(T)` (protected hook returning an error or null, default none) and capacity rules; `exit` unchanged. The generic parameter is functional: it fixes the class token and the parameter type of `admit`/`checkAdditional`, so a subclass cannot even express admission of the wrong kind.
- **Subclasses**: `GeneralAttraction extends Attraction<Ticket>`, `IndividualAttraction extends Attraction<IndividualTicket>`, `GroupAttraction extends Attraction<GroupTicket>`, `SpecialAttraction extends Attraction<IndividualTicket>` (separate class so the next change can override `checkAdditional` for Special Passes without a separate ticket hierarchy).
- **`AttractionType`** enum (`GENERAL`, `INDIVIDUAL`, `GROUP`, `SPECIAL`) with `create(id, name, capacity)` factory and `parse(String)` (exact uppercase); `Attraction.getType()`.
- **Collections**: `AttractionManager` keeps `List<Attraction<?>>`.
- **Persistence**: write `Id;Nombre;Capacidad;Tipo`; read 3 fields as GENERAL, 4 fields with a known type, otherwise skip with warning.
- **Command**: `ATTRACTION CREATE` accepts 3 or 4 parameters; HELP catalog gets a second line for the typed form.
- **Rejection message**: `Attraction A02 (GROUP) does not admit ticket T001.`

## Risks / Trade-offs

- [Raw `Class` tokens and wildcard lists] → contained in the model; casts are checked via `Class.cast`.
