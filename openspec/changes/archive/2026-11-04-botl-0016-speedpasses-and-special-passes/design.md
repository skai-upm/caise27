# Design

## Context

- `IndividualTicket` (direct, held by `TicketManager.tickets`, or constituent, inside `GroupTicket.parts`) stores `price`.
- Constituent tickets currently store the undiscounted base price. `GroupTicket` stores `basePrice` and the discounted `totalPrice`, and `grouptickets.txt` persists both plus member DNIs.
- Services are injected by `App`.
- `ParkController` (Singleton) orchestrates cross-entity rules.
- The invoice builder has the steps header → ticket section → total. A pass step was deferred.

## Goals / Non-Goals

**Goals:**
- One source of truth for a pass's ticket and price: the `IndividualTicket` object and its stored price.
- All checks before any mutation (atomic purchases).

**Non-Goals:**
- Consuming passes at attractions (a later change) and cascade deletion (a later change).

## Decisions

- **Model (`business.model`).** `abstract class Pass { id; price; ticket (IndividualTicket); consumed; markConsumed(); abstract String kind(); }`, with `SpeedPass` and `SpecialPass`. The pass holds the ticket object, never a copy of its data.
- **Prices.**
  - `SpeedPass.priceFor(ticketPrice) = ticketPrice × 0.20`, scale 2, HALF_UP.
  - `SpecialPass.priceFor(ticketPrice) = ticketPrice`.
  - A pack total is the sum of its passes' prices (no discount).
- **Discounted constituent price.** `GroupTicket.memberPrice(base, n) = base × (100 − d(n)) / 100`, scale 2, HALF_UP. It is used when generating parts and when rebuilding them from `grouptickets.txt`, where base and member count are already stored, so the file format is unchanged. The group total keeps its existing formula, so existing totals are byte-identical; with exotic prices, per-member rounding can differ by a cent from total/n. Neither invoices nor existing outputs show constituent prices, so nothing visible changes except pass prices. The brief's acceptance scenario S6 requires pass prices on group members to follow the discounted price, and the source prices passes from "the price stored in its associated individual ticket".
- **Identifiers.** A SpeedPass pack `IdCompra` generates `IdCompra-1…IdCompra-n` (n = 1 or 10, no padding). The Special Pass uses the given identifier. Uniqueness is across all passes. Every generated id is checked before creating any pass.
- **Ticket eligibility.** The ticket is resolved with the new `TicketManager.findIndividualTicket(id)`, which covers direct and constituent tickets but not group tickets. Purchase requires `ticket.isOnOrAfter(today)`, because the brief's scenarios buy passes for valid current or future tickets. Refund requires `ticket.date.isAfter(today) && !consumed`, the same "strictly future" rule as ticket deletion.
- **Special Pass uniqueness.** At most one Special Pass may be *recorded* per ticket, and consumed passes stay recorded. A refunded Special Pass is removed, so the ticket can get a new one (open question below).
- **`PassManager` (`business.service`).**
  - `LinkedHashMap<String, Pass>`, with an injected `PassFileRepository` and `TicketManager` (for lookups).
  - Operations: `buySpeedPasses`, `buySpecialPass`, `refund(id, kind, today)`, `passesOf(IndividualTicket)`, `hasPasses(IndividualTicket)`, `passesOfUser(User)`.
  - Every change is saved, with rollback on `IOException`.
- **Persistence.** `passes.txt` (sibling, git-ignored) with lines `SPEED|SPECIAL;id;ticketId;price;consumed`. It is loaded after tickets, linking via `TicketManager.findIndividualTicket`. Unknown tickets and malformed lines are skipped with a warning.
- **Ticket deletion guard (derived rule).** `ParkController.deleteTicket` rejects a ticket with passes, and `deleteGroupTicket` rejects a group ticket if any part has passes. Message: `Ticket X has passes; refund them first`. The source defers cascade deletion to a later change, so this guard prevents orphan passes in the meantime.
- **Invoices.**
  - `InvoiceBuilder` gains the step `buildPassSection()`, a no-op by default. The director calls it between the ticket section and the total.
  - `UserInvoiceBuilder` receives the user's passes (from `PassManager.passesOfUser`, which includes passes on constituent tickets) and adds the section `Personal passes` with lines `<id> <SpeedPass|SpecialPass> for <ticketId>`.
  - `GroupInvoiceBuilder` keeps the no-op, so member passes never appear there.
- **Commands and output.**
  - Purchases print `OK SPEEDPASS CREATED -> <total>` and `OK SPECIALPASS CREATED -> <precio>`, mirroring the ticket literals.
  - Refunds print `OK: SpeedPass <id> refunded` and `OK: Special Pass <id> refunded`.
  - The quantity must be the literal `1` or `10`.
- **Wiring.** `App` creates `PassFileRepository` and `PassManager(repo, tickets)` and injects `PassManager` into `ParkController.initialize`.
- **UML.**
  - Pass hierarchy.
  - `Pass x--> IndividualTicket : ticket`.
  - `PassManager *--> Pass : passes`.
  - `PassManager x--> PassFileRepository : repository`.
  - `PassManager x--> TicketManager : tickets`.
  - `ParkController x--> PassManager : passes`.
  - `UserInvoiceBuilder x--> Pass : passes`.

## Risks / Trade-offs

- [Constituent price change touches an earlier requirement] → the modification is explicit in the `group-tickets` delta and has no visible effect on prior outputs.
- [Ticket deletion guard is stricter than strictly required] → it is removed or replaced when cascade deletion is specified.

## Open Questions

- Whether a refunded Special Pass still counts towards the one-per-ticket lifetime limit. The source's "even if it has subsequently been used" covers consumption only, so a refund frees the slot.
