# Design

## Context

`InvoiceController.createUserInvoice` collects direct `IndividualTicket`s and calls `InvoiceDirector.userInvoice(user, tickets)`; passes live in `ParkController` (`passesOf(IndividualTicket)`).

## Goals / Non-Goals

**Goals:** passes on user invoices; Delivery 3 integration evidence. **Non-Goals:** USER commands.

## Decisions

- **`UserInvoiceBuilder.addSpeedPasses(List<SpeedPass>)`** and **`addSpecialPasses(List<SpecialPass>)`**: one charged line per pass (`SpeedPass <id> (ticket <ticketId>)`), sections "SpeedPasses" / "Special Passes", omitted when empty — the extension point foreseen in F3-C02.
- **`InvoiceDirector.userInvoice(user, directTickets, passes)`** calls the steps in order: user, individual tickets, SpeedPasses, Special Passes.
- **`InvoiceController`** gathers the passes of every individual ticket of the user (direct tickets and the user's components of group tickets) via `ParkController.passesOf`.
- **Tests**: `Delivery3IntegrationTest` (processor-level full flow incl. invoices, HELP checks) and a packaged-JAR run with `input_fase3.txt` in both modes.

## Risks / Trade-offs

- [Director signature change] → only `InvoiceController` and tests call it.
