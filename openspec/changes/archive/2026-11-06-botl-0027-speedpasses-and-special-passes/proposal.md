# Proposal

## Why

Milestone F3-C06 (Delivery 3, step 6) introduces two personal extras attached to individual tickets — SpeedPasses and Special Passes — with their purchase, pricing, consumption state and refunds, and makes ticket refunds take their passes with them.

## What Changes

- Common pass abstraction: identifier (own identifier space shared by both pass types, not with tickets), associated individual ticket (direct or a group-ticket constituent), price derived from the ticket's stored price, and a consumed flag; consumed passes stay in the system.
- `SPEEDPASS CREATE;<purchase>;<ticket>;<quantity>`: quantity 1 or 10, passes `<purchase>:1..n`, each at 20% of the ticket price, all identifiers checked first (no partial package), total printed (no package discount).
- `SPECIALPASS CREATE;<id>;<ticket>`: at most one per individual ticket, priced as the ticket, price printed.
- Purchases require an existing individual ticket dated on or after the current date.
- `SPEEDPASS DELETE;<pass>` / `SPECIALPASS DELETE;<id>`: refund an unconsumed pass whose ticket is dated strictly after the current date; packaged SpeedPasses are refunded one by one.
- `TICKET DELETE` / `GROUPTICKET DELETE` (when allowed) also remove every pass of the deleted individual tickets, without partial deletions.
- User invoices gain SpeedPass and Special Pass sections (including passes on group-ticket constituents); group invoices do not include passes.
- `HELP` lists the four pass commands.

### In scope
- Source sections 9, 9.1–9.5, and the pass parts of section 6.1.

### Out of scope
- Consuming passes at attraction entry (F3-C08); pass persistence (F3-C09).

## Capabilities

### New Capabilities
- `passes`: SpeedPass and Special Pass purchase, pricing, consumption state, refunds and cascade on ticket deletion.

### Modified Capabilities
- `invoices`: user invoices include the user's passes.
- `execution-control`: `HELP` lists the pass commands.

## Impact

- New `Pass`, `SpeedPass`, `SpecialPass` (business.model), `PassRepository`, `PassService` (singleton), `PassException`; `TicketService.findIndividual` (includes constituents); `ParkController` cascades ticket deletions; `InvoiceService` pass sections; catalog/README.
