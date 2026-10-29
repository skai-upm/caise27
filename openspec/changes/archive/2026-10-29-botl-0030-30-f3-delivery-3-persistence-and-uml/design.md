# Design

## Context

Passes live only in `PassRepository`; their price is computed from the ticket. Storages are configured by `DataLocation` and grouped in `ParkStorage`; `ParkController.loadInto/saveState` handle them. See proposal.md.

## Goals / Non-Goals

**Goals:** complete persistence of Delivery 3 state; diagram matching code.
**Non-Goals:** new behavior.

## Decisions

- **Stored price**: `Pass` gets a final `price` set at construction; `SpeedPass(id, ticket)` / `SpecialPass(id, ticket)` compute it from the ticket price; `(id, ticket, price, consumed)` constructors rebuild loaded passes.
- **`passes.txt`** lines: `S;<id>;<ticketId>;<price>;<consumed>` and `P;<id>;<ticketId>;<price>;<consumed>`; `PassFileStorage.load(ticketById, warnings)` resolves tickets via `TicketRepository.findIndividualById` (covers composition tickets).
- **Load order**: users → groups → attractions → tickets → passes. **Save**: groups, tickets, attractions, passes; `ATTRACTION ENTER` added to the state-changing commands.
- **UML**: add `<<singleton>>` stereotypes to the controllers, `ParkInitializer`/`ParkDependencies`/`ParkStorage`/`PassFileStorage`, help strategy classes, specific pass multiplicities.

## Risks / Trade-offs

- [Prices stored redundantly with the ticket price] → intended: the statement requires the stored price of each pass.
