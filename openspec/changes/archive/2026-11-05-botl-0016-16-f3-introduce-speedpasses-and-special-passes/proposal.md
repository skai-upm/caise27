# Proposal

## Why

Individual tickets have no optional add-ons. Visitors need personal passes attached to individual tickets — SpeedPasses (single or packs of ten) and at most one Special Pass per ticket — each with its own price, consumption status, persistence and refund rules.

## What Changes

- New pass model: abstract pass (identifier, price, associated individual ticket, consumed status) specialized by SpeedPass and SpecialPass.
- `SPEEDPASS CREATE;<IdCompra>;<IdTicket>;<Cantidad>` with quantity `1` or `10`: creates that many independent SpeedPasses with identifiers `<IdCompra>-1` … `<IdCompra>-<n>`, each priced at 20% of the ticket's stored price; all-or-nothing on identifier conflicts.
- `SPECIALPASS CREATE;<Id>;<IdTicket>`: at most one Special Pass per individual ticket (even if consumed), priced at 100% of the ticket's stored price.
- Passes attach to any individual ticket — bought directly or a constituent of a group ticket — whose date is on or after the system date. Purchases report the amount charged.
- `SPEEDPASS DELETE;<IdPase>` / `SPECIALPASS DELETE;<IdPase>`: refund only an unconsumed pass whose ticket date is strictly after the system date. Consumed passes stay recorded and are never refundable.
- Passes persist in `passes.txt` (type, identifier, ticket, price, consumed) and are restored on restart (also under `--fly`).
- The user invoice gains the personal-passes section deferred by the invoice change; HELP/HELP EXTENDED list the new commands; UML updated.
- Out of scope (later changes): consuming passes at attraction entry; cascade deletion of passes when tickets are deleted.

**Divergence flagged:** the brief's scenario S6 assumes a constituent ticket of a group purchase stores a discounted price. In this system constituent tickets store the undiscounted individual price (the discount applies to the group total, per E2), and the authoritative source prices passes from "the price stored in its associated individual ticket"; that stored price is used.

The source suggests change ID `introduce-speedpass-and-special-pass`; this batch uses the BOTL ID `botl-0016-16-f3-introduce-speedpasses-and-special-passes`. Mandatory milestone message: `[F3-C06] Add speed and special passes`.

## Capabilities

### New Capabilities
- `ticket-passes`: SpeedPass and Special Pass purchase, pricing, consumption status, refunds and persistence.

### Modified Capabilities
- `invoice-generation`: user invoices list the user's personal passes.
- `application-control`: HELP lists the four pass commands.

## Impact

- Model `Pass`, `SpeedPass`, `SpecialPass`; business `PassManager` (singleton, configured by `ApplicationContext`); persistence `PassRepository`/`PassFileStore`; `TicketManager.findIndividualTicket`; invoice builder gains a pass step; command parsing, catalog entries, UML.
