## Context

- `Attraction` is a concrete class (id, name, capacity, occupants).
- `AttractionManager.enter` resolves the attraction and ticket, checks date, already-inside and capacity, and admits `ticket.getHolders()` all-or-nothing.
- Tickets are `Ticket` (abstract), `IndividualTicket` and `GroupTicket`.
- Passes are in `PassRepository` (persistence). The model must not import persistence (`LayeredArchitectureTest`).
- `UmlModelConsistencyTest` requires every model, business and persistence type in the diagram. It parses `interface`, `enum` and `abstract class` declarations, reading the name up to the first non-identifier character, so `Attraction<T ...>` parses as `Attraction`.

## Goals / Non-Goals

**Goals:**
- Four types with the correct acceptance rules.
- Interfaces plus bounded generics whose parameter really restricts which tickets an attraction handles.
- The Special Pass condition.
- Creation compatible with Delivery 2, and persistence of the type.

**Non-Goals:**
- The `SPEED` option and pass consumption (F3-C08).
- Showing the type in `ATTRACTION LIST` and `ATTRACTION STATUS`; they are unchanged to keep earlier outputs stable.

## Decisions

1. **Capabilities.**
   - `GeneralAccess` exposes `getId()`, `getDate()`, `getHolders()` and `individualTickets()`: the individual tickets whose passes matter on entry, which is the ticket itself or a group's composition.
   - `IndividualAccess` adds `individualTicket()`.
   - `GroupAccess` adds `getComposition()`.
   - `Ticket implements GeneralAccess`; `IndividualTicket implements IndividualAccess` (`individualTicket()` returns `this`); `GroupTicket implements GroupAccess`.
   - `individualTickets()` is a default method in the two sub-interfaces.
2. **Generic attraction.** `abstract class Attraction<T extends GeneralAccess>` with:
   - `protected Attraction(String id, String name, int capacity, Class<T> accessType)`;
   - `T accept(GeneralAccess ticket)`, which returns `accessType.cast(ticket)` when it is an instance and null otherwise. This is the only way to obtain a ticket typed for the attraction;
   - `String entryRefusal(T ticket, PassLookup passes)`, which returns null when entry is allowed and by default allows it. Because it takes `T`, a special attraction can call `individualTicket()` only because `T` is bound to `IndividualAccess`;
   - `abstract AttractionType getType()`.

   The occupant handling is unchanged. A returned refusal message keeps the model free of business exception types.
3. **`AttractionType`** is an enum with `create(id, name, capacity)` returning the subclass, and `fromName(String)`, which returns null for unknown names (exact, upper-case names).
4. **Manager.**
   - `create(id, name, capacityText, typeText)`, where a `null` type means GENERAL.
   - `enter` resolves the attraction and ticket, then delegates to a private generic `<T extends GeneralAccess> List<User> admit(Attraction<T> a, Ticket ticket)` through wildcard capture. It runs, in order:
     1. `accept`: null leads to `RuleViolationException("attraction A02 (INDIVIDUAL) does not accept group tickets")`;
     2. the date check;
     3. `entryRefusal`;
     4. the not-inside entrants check;
     5. the capacity check.
   - It then adds the holders all-or-nothing.
5. **Persistence.** The save format is `id;name;capacity;TYPE`. On load, a 3-field line is GENERAL; a 4-field line needs a valid type name, otherwise it is malformed. `AttractionRepository` stores `Attraction<?>`.
6. **Command.**
   - `ATTRACTION CREATE` accepts 4 or 5 elements; an empty or unknown type is an error.
   - The response is "OK: attraction A01 (Dragon Rojo) created with capacity 20, type GENERAL".
   - The catalog syntax becomes `ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]`, and its purpose lists the types.
7. **UML.**
   - The three interfaces, with interface inheritance and realizations by the ticket classes.
   - `abstract class Attraction<T extends GeneralAccess>` with its four subclasses, each annotated with its binding (for example `<<bind T=IndividualAccess>>`).
   - `AttractionType`, `PassLookup` and the `PassRepository` realization.

## Risks / Trade-offs

- Response text for `ATTRACTION CREATE` changes (the type is added), so the regression output differs on those lines only.
- A Special Pass can be reused until F3-C08 adds consumption. This is temporary and documented.
