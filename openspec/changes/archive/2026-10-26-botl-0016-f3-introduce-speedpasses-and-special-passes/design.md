# Design

## Context

`IndividualTicket` (holder, price) and `GroupTicket` (composed `IndividualTicket`s with ids `<id>-<n>`) are managed by the `TicketManager` singleton; standalone tickets are found with `findIndividual`, internal ones only through their group ticket. Controllers are singletons initialised in `Bootstrap`; `CommandProcessor` already has 7 constructor parameters.

## Goals / Non-Goals

**Goals:**
- Pass model composed into individual tickets; independent objects with own price/status.
- Purchases, refunds, persistence, invoice section.

**Non-Goals:**
- Consuming passes at attractions (next proposals provide the consume operation's callers); cascade deletion of passes when tickets are refunded is formalised in a later proposal (passes of a deleted ticket disappear with it in memory and orphan lines are ignored on load).

## Decisions

- **Model**: `abstract PersonalPass` (`id`, `price`, `consumed`; `consume()`), `SpeedPass`, `SpecialPass`. `IndividualTicket` composes `List<SpeedPass> speedPasses` and `SpecialPass specialPass` with methods to add/remove and `getPasses()`.
- **Pricing**: SpeedPass = ticket price × 0.20 (scale 2, HALF_UP); Special = ticket price.
- **Ids**: SpeedPasses `<ticketId>-S<n>` with `n` = highest existing number + 1 for that ticket; Special `<ticketId>-SPECIAL`.
- **Special Pass limit**: at most one existing Special Pass per ticket; a consumed one keeps the slot taken. A refunded one is removed (the purchase is cancelled), so the ticket may buy a new one — recorded assumption, since a refund undoes the purchase.
- **`PassManager` singleton** (`initialize(PassRepository, TicketManager, SystemDate)`): create/delete operations, `consume(pass)` for later attraction use, persistence of all passes of all individual tickets. Looks tickets up through new `TicketManager.findAnyIndividual(id)` (standalone or internal) and `allIndividualTickets()`.
- **Persistence**: `PassRepository` interface + `TextFilePassRepository`; `load(Function<String, IndividualTicket>)` attaches passes to tickets, ignoring unknown tickets and malformed lines.
- **Invoices**: `TicketManager.individualTicketsOf(user)` (standalone + internal); `InvoiceManager` adds a `Personal passes` section with each pass id and price.
- **`Controllers` holder** (`business.controller`): immutable bundle of the six controllers built in `Bootstrap`; `CommandProcessor(Controllers, SystemDate, Map<String, HelpGenerator>)`.

## Risks / Trade-offs

- [Pass refunds use the ticket date, which group tickets also follow] → Same rule as ticket refunds, as requested.
